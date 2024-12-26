package br.com.adamis.executions.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ForeignKeysResponse {
	
	private String fkColumn;
	private String referencedTable;
	private String referencedColumn;
	private String referencedColumnType;    
	private Integer referencedColumnLength;
    private Integer referencedColumnPrecision;
    private Integer referencedColumnScale;
	
}
