package com.jsh.erp.datasource.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AccountHeadVo4ListEx extends AccountHead{

    private String itemNames;

    private String organName;

    private String handsPersonName;

    private String userName;

    private String accountName;

    private String billTimeStr;
}