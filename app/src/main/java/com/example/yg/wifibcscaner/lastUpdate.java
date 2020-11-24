package com.example.yg.wifibcscaner;

public class lastUpdate {
    public static final String TABLE = "lastUpdate";
    public static final String COLUMN_tableName = "tableName";
    public static final String COLUMN_updateStart = "updateStart";
    public static final String COLUMN_updateEnd = "updateEnd";
    public static final String COLUMN_updateSuccess = "updateSuccess";

    private String tableName;
    private Long updateStart;
    private Long updateEnd;
    private Boolean updateSuccess = false;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Long getUpdateStart() {
        return updateStart;
    }

    public void setUpdateStart(Long updateStart) {
        this.updateStart = updateStart;
    }

    public Long getUpdateEnd() {
        return updateEnd;
    }

    public void setUpdateEnd(Long updateEnd) {
        this.updateEnd = updateEnd;
    }

    public Boolean getUpdateSuccess() {
        return updateSuccess;
    }

    public void setUpdateSuccess(Boolean updateSuccess) {
        this.updateSuccess = updateSuccess;
    }

    public lastUpdate(String tableName, Long updateStart, Long updateEnd, Boolean updateSuccess) {

        this.tableName = tableName;
        this.updateStart = updateStart;
        this.updateEnd = updateEnd;
        this.updateSuccess = updateSuccess;
    }
}
