INSERT INTO tn_wfm_wfdef (wfdefid, wfdefcd, wfdefnm, wfdeftyp, refid, reftyp, nt, sta, lmt) VALUES (3, 'LEAVE_FLOW_ADV', '请假流程-高级', 'LEAVE', 'LEAVE', 'BUSINESS', '复杂审批流程（含条件+并行+汇聚）', 'A', CURRENT_TIMESTAMP);

INSERT INTO tn_wfm_wfact (wfactid, wfactnm, wfacttyp, rlenm, optyp, dyntyp, rleid, rletyp, reqd, optl, stsval) VALUES (21, '提交申请', 'NODE', '员工', 'PROC', 'SEQU', 'EMP', 'ROLE', 1, '1', 'SUB');
INSERT INTO tn_wfm_wfact (wfactid, wfactnm, wfacttyp, rlenm, optyp, dyntyp, rleid, rletyp, reqd, optl, stsval) VALUES (22, '直属领导审批', 'NODE', '领导', 'PROC', 'SEQU', 'LEADER', 'ROLE', 1, '1', 'APR1');
INSERT INTO tn_wfm_wfact (wfactid, wfactnm, wfacttyp, rlenm, optyp, dyntyp, rleid, rletyp, reqd, optl, stsval) VALUES (23, '金额判断', 'NODE', '系统', 'COND', 'SEQU', 'SYS', 'ROLE', 1, '1', 'COND');
INSERT INTO tn_wfm_wfact (wfactid, wfactnm, wfacttyp, rlenm, optyp, dyntyp, rleid, rletyp, reqd, optl, stsval) VALUES (24, '财务审批', 'NODE', '财务', 'PROC', 'PARA', 'FIN', 'ROLE', 1, '*', 'APR2');
INSERT INTO tn_wfm_wfact (wfactid, wfactnm, wfacttyp, rlenm, optyp, dyntyp, rleid, rletyp, reqd, optl, stsval) VALUES (25, '总监审批', 'NODE', '总监', 'PROC', 'SEQU', 'DIR', 'ROLE', 1, '1', 'APR3');
INSERT INTO tn_wfm_wfact (wfactid, wfactnm, wfacttyp, rlenm, optyp, dyntyp, rleid, rletyp, reqd, optl, stsval) VALUES (26, 'HR审批', 'NODE', 'HR', 'PROC', 'PARA', 'HR', 'ROLE', 1, '*', 'APR4');
INSERT INTO tn_wfm_wfact (wfactid, wfactnm, wfacttyp, rlenm, optyp, dyntyp, rleid, rletyp, reqd, optl, stsval) VALUES (27, '汇聚处理', 'NODE', '系统', 'PROC', 'SEQU', 'SYS', 'ROLE', 1, '1', 'MERGE');
INSERT INTO tn_wfm_wfact (wfactid, wfactnm, wfacttyp, rlenm, optyp, dyntyp, rleid, rletyp, reqd, optl, stsval) VALUES (28, '结束', 'NODE', '系统', 'PROC', 'SEQU', 'SYS', 'ROLE', 1, '1', 'END');

INSERT INTO tv_wfm_wfactconn (wfdefid, curtactid, prevactid, nxtactid, trigcond, optl) VALUES (3, 21, 0, 22, NULL, '1');
INSERT INTO tv_wfm_wfactconn (wfdefid, curtactid, prevactid, nxtactid, trigcond, optl) VALUES (3, 22, 21, 23, NULL, '1');

-- 条件分支
INSERT INTO tv_wfm_wfactconn (wfdefid, curtactid, prevactid, nxtactid, trigcond, optl) VALUES (3, 23, 22, 24, 'amount > 5000', '*');
INSERT INTO tv_wfm_wfactconn (wfdefid, curtactid, prevactid, nxtactid, trigcond, optl) VALUES (3, 23, 22, 26, 'amount <= 5000', '*');

-- 财务线
INSERT INTO tv_wfm_wfactconn (wfdefid, curtactid, prevactid, nxtactid, trigcond, optl) VALUES (3, 24, 23, 25, NULL, '1');
INSERT INTO tv_wfm_wfactconn (wfdefid, curtactid, prevactid, nxtactid, trigcond, optl) VALUES (3, 25, 24, 27, NULL, '1');

-- HR线
INSERT INTO tv_wfm_wfactconn (wfdefid, curtactid, prevactid, nxtactid, trigcond, optl) VALUES (3, 26, 23, 27, NULL, '1');

-- 汇聚
INSERT INTO tv_wfm_wfactconn (wfdefid, curtactid, prevactid, nxtactid, trigcond, optl) VALUES (3, 27, 25, 28, NULL, '1');
INSERT INTO tv_wfm_wfactconn (wfdefid, curtactid, prevactid, nxtactid, trigcond, optl) VALUES (3, 27, 26, 28, NULL, '1');

-- 结束
INSERT INTO tv_wfm_wfactconn (wfdefid, curtactid, prevactid, nxtactid, trigcond, optl) VALUES (3, 28, 27, NULL, NULL, '1');
