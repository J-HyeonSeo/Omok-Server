package com.jhsfully.omokserver.service.impl;

import com.jhsfully.omokserver.dao.PlayerRepository;
import com.jhsfully.omokserver.dao.RoomRepository;
import com.jhsfully.omokserver.dto.RoomDetailDto;
import com.jhsfully.omokserver.dto.request.PutPieceRequestDto;
import com.jhsfully.omokserver.entity.Player;
import com.jhsfully.omokserver.entity.Room;
import com.jhsfully.omokserver.service.GameService;
import com.jhsfully.omokserver.type.Piece;
import com.jhsfully.omokserver.type.State;
import com.jhsfully.omokserver.vo.FindPieceResult;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

    private final RoomRepository roomRepository;
    private final PlayerRepository playerRepository;
    //총 4개의 그룹으로 나누어서 방향을 지정할 것.
    private static final int[][][] DIRS = {{{-1, 0}, {1, 0}}, {{0, -1}, {0, 1}}, {{-1, -1}, {1, 1}}, {{-1, 1}, {1, -1}}};
    private static final int DISCONNECT_LIMIT_SECOND = 3;
    private static final int TURN_TIME = 20;

    @Override
    public RoomDetailDto getGameData(String roomId, String playerId) {

        log.info("{}가 진입하였습니다.", playerId);

        Room room = roomRepository.findById(roomId).orElseThrow();
        Player player = playerRepository.findById(playerId).orElseThrow();

        validateGameData(room, playerId);

        // 최근 연결된 시각을 업데이트 하여, 클라이언트의 연결 상태를 업데이트.
        player.updateLastConnectedAt(LocalDateTime.now());
        playerRepository.save(player);

        // 상대방 정보 가져오기.
        Player otherPlayer = getOtherPlayer(room, playerId);
        String playerName = "";

        // 상대방에 대한 통신상태 확인하기.
        if (otherPlayer != null) {
            playerName = otherPlayer.getPlayerName();
            long otherPlayerConnectedDiff = ChronoUnit.SECONDS.between(
                otherPlayer.getLastConnectedAt(), LocalDateTime.now());

            log.info(otherPlayerConnectedDiff + "");

            if (otherPlayerConnectedDiff >= DISCONNECT_LIMIT_SECOND) {
                room.setNowState(State.DISCONNECTED);
            }
        }

        Room updatedRoom = roomRepository.save(room);

        return RoomDetailDto.of(updatedRoom,
            playerName);
    }

    @Override
    public RoomDetailDto putPiece(PutPieceRequestDto pieceRequestDto, String playerId) {
        Room room = roomRepository.findById(pieceRequestDto.getRoomId()).orElseThrow();

        // 오목돌을 둘 자격이 있는지 검증.
        validatePutPiece(playerId, room);

        // 현재 돌 상태 조회.
        Piece nowPiece = room.getNowState() == State.BLACK ? Piece.BLACK : Piece.WHITE;
        State nextState = room.getNowState() == State.BLACK ? State.WHITE : State.BLACK;

        // 오목돌을 두는 작업 수행.
        int row = pieceRequestDto.getRow();
        int col = pieceRequestDto.getCol();

        if (room.getBoard()[Room.IX(row, col)] != Piece.NONE) {
            throw new RuntimeException("이미 해당 자리에는 돌이 존재합니다.");
        }

        // 결과 판정
        boolean result = isWin(room.getBoard(), nowPiece, row, col);

        // 승자 일 경우 WinnerId를 할당함.
        if (result) {
            room.setWinnerPlayerId(playerId);
        } else if (nowPiece == Piece.BLACK) {

            // 3*3을 체크 해야 함.
            boolean isSamSam = isSamSam(room.getBoard(), row, col);

            // 4*4를 체크 해야 함.
            boolean isSaSa = isSaSa(room.getBoard(), row, col);

            // 둘 중 하나라도 해당 될 경우..
            if (isSamSam || isSaSa) {
                throw new RuntimeException("흑돌은 3*3 또는 4*4를 둘 수 없습니다.");
            }
        }

        // 오목돌을 두기
        room.getBoard()[Room.IX(row, col)] = nowPiece;

        // 턴 넘기기 설정
        room.setNowState(nextState);
        room.setTurnedAt(LocalDateTime.now());

        // 방 정보 Redis에 업데이트
        Room updatedRoom = roomRepository.save(room);

        // 결과 리턴
        return RoomDetailDto.of(updatedRoom,
            Objects.requireNonNull(getOtherPlayer(room, playerId)).getPlayerId());
    }

    private void validatePutPiece(String playerId, Room room) {
        if (room.getPlayerIdList().stream().noneMatch(x -> x.equals(playerId))) {
            throw new RuntimeException("해당 게임방에 대한 참가자 아닙니다.");
        }

        // 남은 시간을 초 단위로 계산하기.
        long diffSeconds = ChronoUnit.SECONDS.between(room.getTurnedAt(), LocalDateTime.now());

        log.info("시간 차이 => {}", diffSeconds);

        // 현재 턴 타임 안에 있는 경우
        if (diffSeconds <= TURN_TIME) {

            String nowPlayerId = room.getNowState() == State.BLACK ? room.getBlackPlayerId() : room.getWhitePlayerId();

            if (!playerId.equals(nowPlayerId)) {
                throw new RuntimeException("현재 턴이 아닙니다.");
            }

        } else { // 턴 타임이 초과 된 경우, 반대로 고려 할 것!

            String nowPlayerId = room.getNowState() == State.WHITE ? room.getBlackPlayerId() : room.getWhitePlayerId();

            // State 업데이트
            room.setNowState(room.getNowState() == State.BLACK ? State.WHITE : State.BLACK);
            roomRepository.save(room);

            if (!playerId.equals(nowPlayerId)) {
                throw new RuntimeException("현재 턴이 아닙니다.");
            }

        }
    }

    private void validateGameData(Room room, String playerId) {
        if (room.getPlayerIdList().stream().noneMatch(it -> it.equals(playerId))) {
            throw new RuntimeException("현재 참가한 게임방이 아닙니다.");
        }
    }

    private Player getOtherPlayer(Room room, String playerId) {
        if (room.getPlayerIdList().size() <= 1) {
            return null;
        }

        String otherPlayerId = room.getPlayerIdList().stream()
            .filter(id -> !id.equals(playerId))
            .findFirst()
            .orElse("");

        if (!StringUtils.hasText(otherPlayerId)) return null;

        return playerRepository.findById(otherPlayerId).orElseThrow();

    }

    /*
        ##########################################################################################
        ######################        GAME VALIDATION LOGIC      #################################
        ##########################################################################################
    */

    // 승리 판별 로직. (흑돌은 장목을 둘 수 없음. (육목 이상 불가능))
    private boolean isWin(Piece[] board, Piece nowPiece, int row, int col) {
        for (int[][] directionGroup : DIRS) {

            int[] firstDir = directionGroup[0];
            int[] secondDir = directionGroup[1];

            int pieceCount = 1;
            pieceCount += findForwardedConnectedPieceCount(board, nowPiece, true, row, col, firstDir[0], firstDir[1]).getCount();
            pieceCount += findForwardedConnectedPieceCount(board, nowPiece, true, row, col, secondDir[0], secondDir[1]).getCount();

            // 흑돌이라면, 정확히 오목을 구성해야 이길 수 있음.
            if (pieceCount == 5 && nowPiece == Piece.BLACK) {
                return true;
            }

            // 백돌이라면, 5~9목을 달성해도 이길 수 있음.
            if (pieceCount >= 5 && pieceCount <= 9 && nowPiece == Piece.WHITE) {
                return true;
            }

        }

        return false;
    }

    // 흑돌은 렌주룰에 의거하여 3*3을 둘 수 없음.
    private boolean isSamSam(Piece[] board, int row, int col) {

        int onceThreeCnt = 0;

        for (int[][] directionGroup : DIRS) {
            int[] firstDir = directionGroup[0];
            int[] secondDir = directionGroup[1];

            FindPieceResult[] candidateResult = new FindPieceResult[2];

            // 첫 번째 결과 구하기.
            FindPieceResult firstPieceResult = findForwardedConnectedPieceCount(board, Piece.BLACK, true, row, col, firstDir[0], firstDir[1]);
            FindPieceResult firstPieceResultWithOneSpace = findForwardedConnectedPieceCount(board, Piece.BLACK, false, row, col, firstDir[0], firstDir[1]);

            // 두 번째 결과 구하기.
            FindPieceResult secondPieceResult = findForwardedConnectedPieceCount(board, Piece.BLACK, true, row, col, secondDir[0], secondDir[1]);
            FindPieceResult secondPieceResultWithOneSpace = findForwardedConnectedPieceCount(board, Piece.BLACK, false, row, col, secondDir[0], secondDir[1]);

            // 결과 조합하기.
            int candidateCnt = 0;

            // 띄우기 없이 전부 하나로 이어진 경우
            if (firstPieceResult.getCount() + secondPieceResult.getCount() == 2) {
                candidateResult[0] = firstPieceResult;
                candidateResult[1] = secondPieceResult;
                candidateCnt++;
            }

            // 하나는 붙인 경우, 하나는 띄운 경우 && 1번 조합과 갯수가 다른 경우
            if (firstPieceResult.getCount() + secondPieceResultWithOneSpace.getCount() == 2
                && secondPieceResult.getCount() != secondPieceResultWithOneSpace.getCount()) {
                candidateResult[0] = firstPieceResult;
                candidateResult[1] = secondPieceResultWithOneSpace;
                candidateCnt++;
            }

            // 하나는 띄운 경우, 하나는 붙인 경우 && 1번 조합과 갯수가 다른 경우
            if (firstPieceResultWithOneSpace.getCount() + secondPieceResult.getCount() == 2
                && firstPieceResult.getCount() != firstPieceResultWithOneSpace.getCount()) {
                candidateResult[0] = firstPieceResultWithOneSpace;
                candidateResult[1] = secondPieceResult;
                candidateCnt++;
            }

            // 유일한 조합 이외에 또 다른 조합이 있다면, 3에 해당 되지 않음.
            if (candidateCnt != 1) {
                continue;
            }

            // Open33 검증.
            FindPieceResult firstNextPiece = findForwardedNonePiece(board,
                candidateResult[0].getEndRow(), candidateResult[0].getEndCol(), firstDir[0], firstDir[1]);
            FindPieceResult secondNextPiece = findForwardedNonePiece(board,
                candidateResult[1].getEndRow(), candidateResult[1].getEndCol(), secondDir[0], secondDir[1]);

            // 공격할 범위가 미리 막혀있어, 닫힌 33임.
            if (firstNextPiece.getDistance() <= 1 && secondNextPiece.getDistance() <= 1) {
                continue;
            }

            // 거리가 2이하 인데, 돌 색깔이 검은색 일 경우.
            if (firstNextPiece.getDistance() <= 2 && firstNextPiece.getLastPiece() == Piece.BLACK) {
                continue;
            }
            if (secondNextPiece.getDistance() <= 2 && secondNextPiece.getLastPiece() == Piece.BLACK) {
                continue;
            }

            onceThreeCnt++; // 모든 조건을 뚫었다면, Open3 해당되는 그룹임.

        }

        return onceThreeCnt >= 2;
    }

    // 흑돌은 렌주룰에 의거하여 4*4를 둘 수 없음.
    private boolean isSaSa(Piece[] board, int row, int col) {

        for (int[][] directionGroup : DIRS) {
            int[] firstDir = directionGroup[0];
            int[] secondDir = directionGroup[1];
        }

        return false;
    }

    // 기점으로 부터 지정한 방향으로 쭉 연결된 지정된 오목돌의 갯수를 반환하는 함수
    private FindPieceResult findForwardedConnectedPieceCount(Piece[] board, Piece nowPiece,
        boolean isJumped, int row, int col, int dx, int dy) {

        int cnt = 0;
        int lastPieceRow = row;
        int lastPieceCol = col;

        while (true) {
            row += dx;
            col += dy;

            if (row < 0 || row > 14 || col < 0 || col > 14) {
                break;
            }

            if (board[Room.IX(row, col)] == nowPiece) {
                cnt++;
                lastPieceRow = row;
                lastPieceCol = col;
            } else if (board[Room.IX(row, col)] == Piece.NONE && !isJumped) {
                isJumped = true;
            } else {
                break;
            }
        }

        return new FindPieceResult(cnt, lastPieceRow, lastPieceCol);
    }

    // 기점으로 부터 지정한 방향으로 특정 돌이 나오거나 벽이 나올 때 까지 탐색
    private FindPieceResult findForwardedNonePiece(Piece[] board, int row, int col, int dx, int dy) {
        int cnt = 0;
        int lastPieceRow = row;
        int lastPieceCol = col;
        int distance = 0;
        Piece lastPiece = board[Room.IX(row, col)];

        while (true) {
            row += dx;
            col += dy;

            if (row < 0 || row > 14 || col < 0 || col > 14) {
                break;
            }

            distance++;

            if (board[Room.IX(row, col)] == Piece.NONE) {
                lastPiece = Piece.NONE;
                cnt++;
            } else {
                lastPiece = board[Room.IX(row, col)];
                lastPieceRow = row;
                lastPieceCol = col;
                break;
            }
        }

        return new FindPieceResult(cnt, distance, lastPieceRow, lastPieceCol, lastPiece);
    }

}
