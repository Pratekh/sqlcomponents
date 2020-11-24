package org.sqlcomponents.compiler.java.mapper;

import org.sqlcomponents.core.mapper.Mapper;
import org.sqlcomponents.core.model.relational.Column;


public class JavaMapper extends Mapper {

    @Override
    public String getDataType(Column column) {
        switch (column.getJdbcType()) {
            case INTEGER:
                return chooseIntegerType(column);
            case NUMERIC:
                return chooseIntegerType(column);
            case VARCHAR:
                return "java.lang.String";
        }
        throw new RuntimeException("Datatype not found for column "+ column);
    }

    private String chooseIntegerType(Column column) {
        return column.getSize() > 2 ? "java.lang.Integer" : "java.lang.Byte";
    }
}