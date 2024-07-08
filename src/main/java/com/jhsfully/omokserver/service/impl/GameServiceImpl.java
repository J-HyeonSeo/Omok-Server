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
    private static final int DIRECTION_SIZE = 8;
    private static final int SAM_SAM_RANGE = 3;
    private static final int RESULT_CHECK_RANGE = 4;
    private static final int DISCONNECT_LIMIT_SECOND = 3;
    private static final int TURN_TIME = 20;

    @Override
    public RoomDetailDto getGameData(String roomId, String playerId) {

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
            pieceCount += findForwardedConnectedPieceCount(board, Piece.BLACK, row, col, firstDir[0], firstDir[1]);
            pieceCount += findForwardedConnectedPieceCount(board, Piece.BLACK, row, col, secondDir[0], secondDir[1]);

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
        return false;
    }

    // 흑돌은 렌주룰에 의거하여 4*4를 둘 수 없음.
    private boolean isSaSa(Piece[] board, int row, int col) {
        return false;
    }

    // 기점으로 부터 지정한 방향으로 N칸 까지 특정 오목돌의 색깔의 갯수를 반환하는 함수
    private int findForwardedPieceCount(Piece[] board, Piece nowPiece, int row, int col, int dx, int dy) {
        return 0;
    }

    // 기점으로 부터 지정한 방향으로 쭉 연결된 지정된 오목돌의 갯수를 반환하는 함수
    private int findForwardedConnectedPieceCount(Piece[] board, Piece nowPiece, int row, int col, int dx, int dy) {

        int cnt = 0;

        row += dx;
        col += dy;

        if (board[Room.IX(row, col)] == nowPiece) {
            cnt++;
            cnt += findForwardedConnectedPieceCount(board, nowPiece, row, col, dx, dy);
        }

        return cnt;
    }
}
