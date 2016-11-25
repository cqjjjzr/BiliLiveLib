package charlie.bililivelib.net.datamodel;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LiveAddresses {
    private String lineMain;
    private String lineBackup1;
    private String lineBackup2;
    private String lineBackup3;
}
