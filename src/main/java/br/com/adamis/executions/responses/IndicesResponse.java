package br.com.adamis.executions.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IndicesResponse {

	private String tableName;
    private String indexName;
    private String indexType;
    private Boolean isUnique;
    private String indexStatus;
    private String columnName;
    private Integer columnPosition;
    private String descend;
	
}
