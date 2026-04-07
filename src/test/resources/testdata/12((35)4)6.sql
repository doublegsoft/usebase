INSERT INTO tn_wfm_wfdef (wfdefid, wfdefcd, wfdefnm, wfdeftyp, refid, reftyp, nt, sta, lmt) VALUES (2, 'LEAVE_FLOW_PAR', '请假流程-并行', 'LEAVE', 'LEAVE', 'BUSINESS', '并行审批流程', 'A', CURRENT_TIMESTAMP);

INSERT INTO tn_wfm_wfact (wfactid, wfactnm, wfacttyp, rlenm, optyp, dyntyp, rleid, rletyp, reqd, optl, stsval) VALUES (11, '提交申请', 'NODE', '员工', 'PROC', 'SEQU', 'EMP', 'ROLE', 1, '1', 'SUB');
INSERT INTO tn_wfm_wfact (wfactid, wfactnm, wfacttyp, rlenm, optyp, dyntyp, rleid, rletyp, reqd, optl, stsval) VALUES (12, '领导审批', 'NODE', '领导', 'PROC', 'SEQU', 'LEADER', 'ROLE', 1, '1', 'APR1');
INSERT INTO tn_wfm_wfact (wfactid, wfactnm, wfacttyp, rlenm, optyp, dyntyp, rleid, rletyp, reqd, optl, stsval) VALUES (13, '财务审批', 'NODE', '财务', 'PROC', 'PARA', 'FIN', 'ROLE', 1, '*', 'APR2');
INSERT INTO tn_wfm_wfact (wfactid, wfactnm, wfacttyp, rlenm, optyp, dyntyp, rleid, rletyp, reqd, optl, stsval) VALUES (14, 'HR审批', 'NODE', 'HR', 'PROC', 'PARA', 'HR', 'ROLE', 1, '*', 'APR3');
INSERT INTO tn_wfm_wfact (wfactid, wfactnm, wfacttyp, rlenm, optyp, dyntyp, rleid, rletyp, reqd, optl, stsval) VALUES (15, '汇总处理', 'NODE', '系统', 'PROC', 'SEQU', 'SYS', 'ROLE', 1, '1', 'MERGE');
INSERT INTO tn_wfm_wfact (wfactid, wfactnm, wfacttyp, rlenm, optyp, dyntyp, rleid, rletyp, reqd, optl, stsval) VALUES (16, '结束', 'NODE', '系统', 'PROC', 'SEQU', 'SYS', 'ROLE', 1, '1', 'END');

INSERT INTO tv_wfm_wfactconn (wfdefid, curtactid, prevactid, nxtactid, trigcond, optl) VALUES (2, 11, 0, 12, NULL, '1');
INSERT INTO tv_wfm_wfactconn (wfdefid, curtactid, prevactid, nxtactid, trigcond, optl) VALUES (2, 12, 11, 13, NULL, '*');
INSERT INTO tv_wfm_wfactconn (wfdefid, curtactid, prevactid, nxtactid, trigcond, optl) VALUES (2, 12, 11, 14, NULL, '*');
INSERT INTO tv_wfm_wfactconn (wfdefid, curtactid, prevactid, nxtactid, trigcond, optl) VALUES (2, 13, 12, 15, NULL, '1');
INSERT INTO tv_wfm_wfactconn (wfdefid, curtactid, prevactid, nxtactid, trigcond, optl) VALUES (2, 15, 13, 16, NULL, '1');
INSERT INTO tv_wfm_wfactconn (wfdefid, curtactid, prevactid, nxtactid, trigcond, optl) VALUES (2, 14, 12, 16, NULL, '1');
INSERT INTO tv_wfm_wfactconn (wfdefid, curtactid, prevactid, nxtactid, trigcond, optl) VALUES (2, 16, 15, NULL, NULL, '1');
