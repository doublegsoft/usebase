INSERT INTO tn_wfm_wfdef (wfdefid, wfdefcd, wfdefnm, wfdeftyp, refid, reftyp, nt, sta, lmt) VALUES (1, 'LEAVE_FLOW', '请假流程', 'LEAVE', 'LEAVE', 'BUSINESS', '简单请假流程', 'A', CURRENT_TIMESTAMP);

INSERT INTO tn_wfm_wfact (wfactid, wfactnm, wfacttyp, rlenm, optyp, dyntyp, rleid, rletyp, reqd, optl, stsval) VALUES (1, '提交申请', 'NODE', '员工', 'PROC', 'SEQU', 'EMP', 'ROLE', 1, '1', 'SUB');
INSERT INTO tn_wfm_wfact (wfactid, wfactnm, wfacttyp, rlenm, optyp, dyntyp, rleid, rletyp, reqd, optl, stsval) VALUES (2, '直属领导审批', 'NODE', '领导', 'PROC', 'SEQU', 'LEADER', 'ROLE', 1, '1', 'APR1');
INSERT INTO tn_wfm_wfact (wfactid, wfactnm, wfacttyp, rlenm, optyp, dyntyp, rleid, rletyp, reqd, optl, stsval) VALUES (3, 'HR审批', 'NODE', 'HR', 'PROC', 'SEQU', 'HR', 'ROLE', 1, '1', 'APR2');
INSERT INTO tn_wfm_wfact (wfactid, wfactnm, wfacttyp, rlenm, optyp, dyntyp, rleid, rletyp, reqd, optl, stsval) VALUES (4, '结束', 'NODE', '系统', 'PROC', 'SEQU', 'SYS', 'ROLE', 1, '1', 'END');

INSERT INTO tv_wfm_wfactconn (wfdefid, curtactid, prevactid, nxtactid, trigcond, optl) VALUES (1, 1, 0, 2, NULL, '1');
INSERT INTO tv_wfm_wfactconn (wfdefid, curtactid, prevactid, nxtactid, trigcond, optl) VALUES (1, 2, 1, 3, NULL, '1');
INSERT INTO tv_wfm_wfactconn (wfdefid, curtactid, prevactid, nxtactid, trigcond, optl) VALUES (1, 3, 2, 4, NULL, '1');
INSERT INTO tv_wfm_wfactconn (wfdefid, curtactid, prevactid, nxtactid, trigcond, optl) VALUES (1, 4, 3, NULL, NULL, '1');
