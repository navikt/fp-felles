package no.nav.vedtak.felles.db.doc.model;

public class Column {

    private final String columnName;
    private final String columnType;
    private final int columnSize;
    private final String comment;
    private boolean isNullable;
    private String defaultValue;
    private boolean primaryKey;
    private String foreignKeyDefinition;

    public Column(String name, String type, int colSize, String defaultValue, boolean isNullable, String comment,
            boolean primaryKeyColumn) {
        this.columnName = name.toUpperCase();
        this.columnType = type;
        this.columnSize = colSize;
        this.defaultValue = defaultValue;
        this.isNullable = isNullable;
        this.comment = comment;
        this.primaryKey = primaryKeyColumn;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public boolean isNullable() {
        return isNullable;
    }

    public String getComment() {
        return comment;
    }

    public String getName() {
        return columnName;
    }

    public String getType() {
        return columnType;
    }

    public int getSize() {
        return columnSize;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<name=" + columnName + ", type=" + columnType + ">";
    }

    public String getForeignKeyDefinition() {
        return foreignKeyDefinition;
    }

    public boolean isForeignKey() {
        return foreignKeyDefinition != null;
    }
}