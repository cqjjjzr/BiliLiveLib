package charlie.bililivelib.danmaku;

import lombok.Getter;

public class DanmakuReceivePacket {
    public enum Operation {
        PLAYER_COUNT(new int[]{0, 1, 2});

        @Getter
        private int[] id;

        Operation(int[] id) {
            this.id = id;
        }

        public static Operation forID(int id) {
            for (Operation operation : Operation.values()){
                for (int findID : operation.id)
                    if(id == findID) return operation;
            }
            return PLAYER_COUNT;
        }
    }
}
