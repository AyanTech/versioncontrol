package ir.ayantech.testcases;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import ir.ayantech.versioncontrol.VersionControlCore;

public class TestcaseListActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testcase_list);
        bindButtons();
    }

    private void bindButtons() {
        findViewById(R.id.testcase1Button).setOnClickListener(this);
        findViewById(R.id.testcase2Button).setOnClickListener(this);
        findViewById(R.id.testcase3Button).setOnClickListener(this);
        findViewById(R.id.testcase4Button).setOnClickListener(this);
        findViewById(R.id.testcase5Button).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Integer testcaseNumber = getTestcaseNumber(v);
        String testcaseName = getTestcaseName(testcaseNumber);
        runVersionControlTestcase(testcaseName);
    }

    @NonNull
    private Integer getTestcaseNumber(View v) {
        Integer testcaseNumber = 0;
        switch (v.getId()) {
            case R.id.testcase1Button:
                testcaseNumber = 1;
                break;

            case R.id.testcase2Button:
                testcaseNumber = 2;
                break;

            case R.id.testcase3Button:
                testcaseNumber = 3;
                break;

            case R.id.testcase4Button:
                testcaseNumber = 4;
                break;

            case R.id.testcase5Button:
                testcaseNumber = 5;
                break;
        }
        return testcaseNumber;
    }

    @NonNull
    private String getTestcaseName(Integer testcaseNumber) {
        return "testcase" + testcaseNumber;
    }

    private void runVersionControlTestcase(String testcaseName) {
        new VersionControlCore(this)
                .setApplicationName(testcaseName)
                .setCategoryName("cafebazar")
                .checkForNewVersion();
    }
}