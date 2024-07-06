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
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

    private final RoomRepository roomRepository;
    private final PlayerRepository playerRepository;
    private static final int[] DX = {-1, 1, 0, 0, -1, -1, 1, 1};
    private static final int[] DY = {0, 0, -1, 1, -1, 1, -1, 1};
    private static final int DIRECTION_SIZE = 8;
    private static final int SAM_SAM_RANGE = 3;
    private static final int RESULT_CHECK_RANGE = 4;
    private static final int DISCONNECT_LIMIT_SECOND = 3;

    @Override
    public RoomDetailDto getGameData(String roomId, String playerId) {

        Room room = roomRepository.findById(roomId).orElseThrow();
        Player player = playerRepository.findById(playerId).orElseThrow();

        validateGameData(room, playerId);

        // 최근 연결된 시각을 업데이트 하여, 클라이언트의 연결 상태를 업데이트.
        player.updateLastConnectedAt(LocalDateTime.now());

        // 상대방 정보 가져오기.
        Player otherPlayer = getOtherPlayer(room, playerId);

        // 상대방에 대한 통신상태 확인하기.
        long otherPlayerConnectedDiff = ChronoUnit.SECONDS.between(
            Objects.requireNonNull(otherPlayer).getLastConnectedAt(), LocalDateTime.now());

        if (otherPlayerConnectedDiff >= DISCONNECT_LIMIT_SECOND ) {
            room.setNowState(State.DISCONNECTED);
        }

        return RoomDetailDto.of(roomRepository.findById(roomId).orElseThrow(),
            otherPlayer.getPlayerName());
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

        // 만약 흑돌 플레이어 인 경우, 33을 두었는지 검증 해야함(렌주룰)
        if (room.getBlackPlayerId().equals(playerId)) {
            validateSamSam(room.getBoard(), row, col);
        }

        // 오목돌을 두기
        room.getBoard()[Room.IX(row, col)] = nowPiece;

        // 결과 판정
        boolean result = checkWinner(room.getBoard(), nowPiece, row, col);

        if (result) {
            room.setWinnerPlayerId(playerId);
        } else {
            room.setNowState(nextState);
            room.setTurnedAt(LocalDateTime.now());
        }

        Room updatedRoom = roomRepository.save(room);

        return RoomDetailDto.of(updatedRoom,
            Objects.requireNonNull(getOtherPlayer(room, playerId)).getPlayerId());
    }

    private boolean checkWinner(Piece[] board, Piece nowPiece, int row, int col) {

        // 본인 방향 중심으로 8방향으로 4칸의 현재 PIECE가 나오는지 확인.
        for (int i = 0; i < DIRECTION_SIZE; i++) {
            int pieceCnt = 0;
            int nowRow = row;
            int nowCol = col;
            for (int j = 0; j < RESULT_CHECK_RANGE; j++) {
                nowRow += DX[i];
                nowCol += DY[i];

                // 범위 확인.
                if (nowRow < 0 || nowCol < 0 || nowRow > 14 || nowCol > 14) {
                    break;
                }

                if (board[Room.IX(nowRow, nowCol)] == nowPiece) {
                    pieceCnt++;
                }

            }

            // 이겼음. 나이스~ :)
            if (pieceCnt == 4) {
                return true;
            }
        }

        return false;
    }

    // 흑돌이 3*3 규칙을 어겼는지 확인하는 밸리데이션.
    private void validateSamSam(Piece[] board, int row, int col) {

        int cnt = 0;

        // 본인 방향 중심으로 8방향으로 다음 3칸 범위까지 2개의 돌이 있으면 체크
        for (int i = 0; i < DIRECTION_SIZE; i++) {
            int blackCnt = 0;
            int nowRow = row;
            int nowCol = col;
            for (int j = 0; j < SAM_SAM_RANGE; j++) {
                nowRow += DX[i];
                nowCol += DY[i];

                // 범위 확인.
                if (nowRow < 0 || nowCol < 0 || nowRow > 14 || nowCol > 14) {
                    break;
                }

                if (board[Room.IX(nowRow, nowCol)] == Piece.BLACK) {
                    blackCnt++;
                }

            }

            // 3*3의 징조가 보임.
            if (blackCnt == 2) {
                cnt++;
            }
        }

        // 3*3을 적발함. 수고링 :)
        if (cnt >= 2) {
            throw new RuntimeException("흑돌은 해당 위치에 둘 수 없습니다.");
        }

    }

    private void validatePutPiece(String playerId, Room room) {
        if (room.getPlayerIdList().stream().noneMatch(x -> x.equals(playerId))) {
            throw new RuntimeException("해당 게임방에 대한 참가자 아닙니다.");
        }

        // 남은 시간을 초 단위로 계산하기.
        long diffSeconds = ChronoUnit.SECONDS.between(room.getTurnedAt(), LocalDateTime.now());

        // 시간 차이가 남아있는 경우
        if (diffSeconds >= 0) {

            String nowPlayerId = room.getNowState() == State.BLACK ? room.getBlackPlayerId() : room.getWhitePlayerId();

            if (!playerId.equals(nowPlayerId)) {
                throw new RuntimeException("현재 턴이 아닙니다.");
            }

        } else { // 음수 인 경우, State가 업데이트 되지 않은 경우 이므로, 반대로 가져오고, State를 업데이트 해야함.

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

}
