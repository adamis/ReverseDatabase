package br.com.adamis.executions.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PrimaryKeyResponse {
	
	private String columnName;
	private String dataType;
	private Integer dataLength;
	
}