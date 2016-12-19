package charlie.bililivelib.datamodel;

import lombok.Data;

@Data
public class Medal {
    private short level;
    private String name;
    private String masterName;
    private int masterRoomID;
}