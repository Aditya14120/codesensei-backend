package com.example.codesensei.service.analyzer;

import com.example.codesensei.model.SupportedLanguage;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PMDAnalyzer {

    public List<String> analyze(String code, SupportedLanguage language) {

        List<String> issues = new ArrayList<>();

        try {

            if (code == null || code.trim().isEmpty()) {
                issues.add("[PMD] Code is empty.");
                return issues;
            }

            switch (language) {
                case PYTHON -> analyzePython(code, issues);
                case C -> analyzeC(code, issues);
                case CPP -> analyzeCpp(code, issues);
                default -> analyzeJava(code, issues);
            }

            if (code.length() > 1000) {
                issues.add("[PMD] Method/class is too large.");
            }

        } catch (Exception e) {

            issues.add("PMD analysis failed: " + e.getMessage());

        }

        if (issues.isEmpty()) {
            issues.add("No PMD issues found.");
        }

        return issues;
    }

    private void analyzeJava(String code, List<String> issues) {

        if (code.contains("System.out.println")) {
            issues.add("[PMD] Avoid using System.out.println in production code.");
        }

        if (code.contains("int x")) {
            issues.add("[PMD] Variable name 'x' is not meaningful.");
        }

        if (code.contains("catch(Exception") || code.contains("catch (Exception")) {
            issues.add("[PMD] Avoid catching generic Exception.");
        }

        if (code.contains("== null")) {
            issues.add("[PMD] Consider better null handling.");
        }
    }

    private void analyzePython(String code, List<String> issues) {

        if (code.contains("print(")) {
            issues.add("[PMD] Avoid using print() for production logging.");
        }

        if (code.matches("(?s).*\\bx\\s*=[^=].*")) {
            issues.add("[PMD] Variable name 'x' is not meaningful.");
        }

        if (code.contains("except:")) {
            issues.add("[PMD] Avoid bare 'except:' clauses; catch specific exceptions.");
        }

        if (code.contains("== None") || code.contains("!= None")) {
            issues.add("[PMD] Use 'is None' / 'is not None' instead of comparing with '=='.");
        }
    }

    private void analyzeC(String code, List<String> issues) {

        if (code.contains("int x")) {
            issues.add("[PMD] Variable name 'x' is not meaningful.");
        }

        if (code.contains("gets(")) {
            issues.add("[PMD] Avoid gets(); it cannot bound input size and risks buffer overflow.");
        }

        if (code.contains("== NULL") || code.contains("!= NULL")) {
            issues.add("[PMD] Consider better null handling.");
        }

        if (!code.contains("#include")) {
            issues.add("[PMD] No headers included; verify all used functions are declared.");
        }
    }

    private void analyzeCpp(String code, List<String> issues) {

        if (code.contains("int x")) {
            issues.add("[PMD] Variable name 'x' is not meaningful.");
        }

        if (code.contains("catch(...)") || code.contains("catch (...)")) {
            issues.add("[PMD] Avoid catching all exceptions with catch(...); catch specific types.");
        }

        if (code.contains("== nullptr") || code.contains("!= nullptr")) {
            issues.add("[PMD] Consider better null handling.");
        }

        if (code.contains("using namespace std;")) {
            issues.add("[PMD] Avoid 'using namespace std;' in headers/large scopes.");
        }
    }
}
