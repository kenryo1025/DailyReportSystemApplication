package com.techacademy.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.entity.Report;
import com.techacademy.repository.ReportRepository;

@Service
public class ReportService {
    private final ReportRepository reportRepository;

    @Autowired
    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    // 日報保存
    @Transactional
    public ErrorKinds save(Report report, @AuthenticationPrincipal UserDetail userDetail, Model model) {

        // 日付の重複度チェック
        ErrorKinds result = checkDate(userDetail,report);
        if (ErrorKinds.CHECK_OK != result) {

        }

        // 現在ログインしている従業員をレポートに設定
        report.setEmployee(userDetail.getEmployee());

        report.setDeleteFlg(false);

        LocalDateTime now = LocalDateTime.now();
        report.setCreatedAt(now);
        report.setUpdatedAt(now);

        reportRepository.save(report);
        return ErrorKinds.SUCCESS;
    }

 // 日報更新
    @Transactional
    public ErrorKinds update(Report report, @AuthenticationPrincipal UserDetail userDetail, Model model) {

        Optional<Report> existingReportOpt = reportRepository.findById(report.getId());

        Report existingReport = existingReportOpt.get();


        // 日付の重複度チェック
        ErrorKinds result = checkDate(userDetail,report);
        if (ErrorKinds.CHECK_OK != result) {
            return result;
        }

        // 既存の情報を更新
        LocalDateTime now = LocalDateTime.now();
        existingReport.setReport_date(report.getReport_date());
        existingReport.setUpdatedAt(now);
        existingReport.setTitle(report.getTitle());
        existingReport.setContent(report.getContent());

        // 更新を保存
        reportRepository.save(existingReport);

        return ErrorKinds.SUCCESS;
    }


    // 日報削除
    @Transactional
    public ErrorKinds delete(Long id, UserDetail userDetail) {

        Report report = findById(id);
        LocalDateTime now = LocalDateTime.now();
        report.setUpdatedAt(now);
        report.setDeleteFlg(true);

        return ErrorKinds.SUCCESS;
    }

    // 従業員一覧表示処理
    public List<Report> findAll() {
        return reportRepository.findAll();
    }

    // 1件を検索
    public Report findById(Long id) {
        // findByIdで検索
        Optional<Report> option = reportRepository.findById(id);
        // 取得できなかった場合はnullを返す
        return option.orElse(null);
    }

    // 日報を重複度をチェック
    public ErrorKinds checkDate(UserDetail userDetail, Report report) {
        // findByEmployeeでログインユーザーの日報を検索
        List<Report> reportlist = reportRepository.findByEmployee(userDetail.getEmployee());
        if (reportlist != null) {
            //ログインしている従業員の保存されている日報と新規登録される日報の日付を確認
            for (Report dup_report : reportlist) {
                // 更新の場合、同じIDの日報はスキップ
                if (dup_report.getId().equals(report.getId())) {
                    return ErrorKinds.CHECK_OK;
                    }
                if (dup_report.getReport_date().equals(report.getReport_date())) {
                   return ErrorKinds.DATECHECK_ERROR;
                }
            }
        }
        return ErrorKinds.CHECK_OK;
    }

}
