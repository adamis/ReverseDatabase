package br.com.adamis.executions.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TablesResponse {

	private String tableName;
	private String columnName;
	private String dataType;
	private Integer dataLength;
	private Integer dataPrecision;
	private Integer dataScale;
	private String nullable;
	private String columnId;

}