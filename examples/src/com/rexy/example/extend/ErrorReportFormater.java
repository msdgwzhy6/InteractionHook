package com.rexy.example.extend;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

/**
 * TODO:格式化异常，截取有用的信息。
 *
 * @author: rexy
 */
public class ErrorReportFormater  {
    private TreeSet<String> mImpossiblePackages = null;
    private String[] mExtraPackages = null;
    private IReportFilter mStackFilter = null;
    private boolean mShowLineNumber = true;
    private boolean mEnsurePackageStackCount = true;
    private boolean mEnsureMaxStackCount = true;
    private int mMaxStackCount = -1;
    private int mMinPackageStackCount = -1;
    private int mSuggestStackCount = -1;

    public static final IReportFilter DEFAULT_FILTER = new IReportFilter() {
        @Override
        public boolean acceptReportStack(String className, String methodName) {
            return !methodName.startsWith("access$");
        }

        @Override
        public String formatReportStack(String threadName, Throwable cause, List<StackTraceElement> elements, boolean showLineNumber) {
            StringBuilder sb = new StringBuilder();
            sb.append(cause.toString());
            if (!TextUtils.isEmpty(threadName)) {
                sb.append(String.format("@{%s}", new Object[]{threadName}));
            }
            int size = elements == null ? 0 : elements.size();
            for (int i = 0; i < size; i++) {
                dumpTraceElement(elements.get(i), sb, showLineNumber);
            }
            return sb.toString();
        }

        protected StringBuilder dumpTraceElement(StackTraceElement e, StringBuilder sb, boolean showLineNumber) {
            sb = sb == null ? new StringBuilder() : sb;
            String className = e.getClassName();
            String methodName = e.getMethodName();
            int lineNumber = e.getLineNumber();
            if (showLineNumber && lineNumber >= 0) {
                sb.append(String.format("\n<(%s:%s:%s)", new Object[]{className, methodName, lineNumber}));
            } else {
                sb.append(String.format("\n<(%s:%s)", new Object[]{className, methodName}));
            }
            return sb;
        }
    };

    public ErrorReportFormater(int suggestStackCount, int maxStackCount, int minPackageStackCount, String... extraPackages) {
        mSuggestStackCount = suggestStackCount;
        mMaxStackCount = maxStackCount;
        mMinPackageStackCount = minPackageStackCount;
        if (extraPackages != null && extraPackages.length > 1) {
            setExtraPackages(extraPackages);
        }
    }

    public ErrorReportFormater setExtraPackages(String... packages) {
        mExtraPackages = packages;
        if (mImpossiblePackages != null) {
            mImpossiblePackages.clear();
            mImpossiblePackages = null;
        }
        return ErrorReportFormater.this;
    }

    public ErrorReportFormater setMaxStackCount(int maxStackCount) {
        mMaxStackCount = maxStackCount;
        return ErrorReportFormater.this;
    }

    public ErrorReportFormater setMinPackageStackCount(int minPackageStackCount) {
        mMinPackageStackCount = minPackageStackCount;
        return ErrorReportFormater.this;
    }

    public ErrorReportFormater setSuggestStackCount(int suggestStackCount) {
        mSuggestStackCount = suggestStackCount;
        return ErrorReportFormater.this;
    }

    public ErrorReportFormater setShowLineNumber(boolean showLineNumber) {
        mShowLineNumber = showLineNumber;
        return ErrorReportFormater.this;
    }

    public ErrorReportFormater setStackCountConstraint(boolean ensurePackageStackCountIfNeed, boolean ensureMaxStackCount) {
        mEnsurePackageStackCount = ensurePackageStackCountIfNeed;
        mEnsureMaxStackCount = ensureMaxStackCount;
        return ErrorReportFormater.this;
    }

    public ErrorReportFormater setStackFilter(IReportFilter filter) {
        mStackFilter = filter;
        return ErrorReportFormater.this;
    }

    public static HashSet<String> getAllImpossiblePackage(Context context, boolean checkActivityName) {
        HashSet<String> packages = new HashSet<String>();
        if (context != null) {
            try {
                String applicationPackage = context.getApplicationContext().getPackageName();
                if (!TextUtils.isEmpty(applicationPackage)) {
                    packages.add(applicationPackage);
                    PackageInfo packageName = context.getApplicationContext().getPackageManager().getPackageInfo(applicationPackage, 15);
                    ActivityInfo[] needToAdd = packageName.activities;
                    if (needToAdd != null && needToAdd.length > 0) {
                        for (ActivityInfo atyInfo : needToAdd) {
                            if (atyInfo != null && !TextUtils.isEmpty(atyInfo.packageName)) {
                                packages.add(atyInfo.packageName);
                                if (checkActivityName) {
                                    String activityName = atyInfo.name;
                                    int point = activityName == null ? -1 : activityName.lastIndexOf('.');
                                    if (point > 0) {
                                        String importName = activityName.substring(0, point);
                                        point = importName.lastIndexOf('.');
                                        if (point > 0) {
                                            packages.add(importName.substring(0, point));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.toString();
            }
        }
        return packages;
    }

    protected TreeSet<String> getImpossiblePackages() {
        if (mImpossiblePackages == null) {
            HashSet<String> packages = getAllImpossiblePackage(MyApplication.getApp(), true);
            if (mExtraPackages != null && mExtraPackages.length > 0) {
                for (String extras : mExtraPackages) {
                    if (!TextUtils.isEmpty(extras)) {
                        packages.add(extras);
                    }
                }
            }
            mImpossiblePackages = new TreeSet<String>();
            Iterator<String> packageIterator = packages.iterator();
            while (packageIterator.hasNext()) {
                String pk = packageIterator.next();
                boolean needAdded = true;
                if (!mImpossiblePackages.isEmpty()) {
                    Iterator<String> addIterator = mImpossiblePackages.iterator();
                    while (addIterator.hasNext()) {
                        String addPk = addIterator.next();
                        if (pk.startsWith(addPk)) {
                            needAdded = false;
                            break;
                        } else {
                            if (addPk.startsWith(pk)) {
                                addIterator.remove();
                            }
                        }
                    }
                }
                if (needAdded) {
                    mImpossiblePackages.add(pk);
                }
            }
        }
        return mImpossiblePackages;
    }

    public static Throwable getLastCause(Throwable t) {
        Throwable result = null;
        if (t != null) {
            for (result = t; result.getCause() != null; result = result.getCause()) ;
        }
        return result;
    }

    protected int[] getStackRangeOption(int stackSize) {
        int[] result = new int[3];
        if (mMaxStackCount <= 0) {
            result[0] = stackSize;
        } else {
            result[0] = Math.min(mMaxStackCount, stackSize);
        }

        if (mSuggestStackCount <= 0) {
            result[1] = Math.min(result[0], 5);
        } else {
            result[1] = Math.min(result[0], mSuggestStackCount);
        }

        if (mMinPackageStackCount < 0) {
            result[2] = Math.min(result[1], 1);
        } else {
            result[2] = Math.min(result[1], mMinPackageStackCount);
        }
        return result;
    }

    protected List<StackTraceElement> getBestStackTraceElement(StackTraceElement[] elements, IReportFilter filter) {
        int stackSize = elements.length, index;
        int[] options = getStackRangeOption(stackSize);
        int maxAcceptCount = options[0];
        int suggestCount = options[1];
        int minPackageCount = options[2];
        List<StackTraceElement> accepts = new ArrayList<StackTraceElement>(suggestCount + 1);
        for (index = 0; index < stackSize; ++index) {
            StackTraceElement e = elements[index];
            String className = e.getClassName();
            String methodName = e.getMethodName();
            if (!TextUtils.isEmpty(className) && !TextUtils.isEmpty(methodName)) {
                if (index == 0 || filter == null || filter.acceptReportStack(className, methodName)) {
                    suggestCount--;
                    maxAcceptCount--;
                    accepts.add(e);
                    if (minPackageCount > 0) {
                        Iterator<String> pkgs = getImpossiblePackages().iterator();
                        while (pkgs.hasNext()) {
                            String packageName = pkgs.next();
                            if (className.startsWith(packageName)) {
                                minPackageCount--;
                            }
                        }
                    }
                    if ((minPackageCount <= 0 && suggestCount <= 0) || maxAcceptCount <= 0) {
                        break;
                    }
                }
            }
        }
        if (mEnsurePackageStackCount && minPackageCount > 0 && index < stackSize - 1) {
            ArrayList<StackTraceElement> pkStack = new ArrayList<>(stackSize - index);
            int lastAddedIndex = -1;
            for (index = index + 1; index < stackSize; ++index) {
                StackTraceElement e = elements[index];
                String className = e.getClassName();
                String methodName = e.getMethodName();
                if (!TextUtils.isEmpty(className) && !TextUtils.isEmpty(methodName)) {
                    if (filter == null || filter.acceptReportStack(className, methodName)) {
                        pkStack.add(e);
                        Iterator<String> pkgs = getImpossiblePackages().iterator();
                        while (pkgs.hasNext()) {
                            String packageName = pkgs.next();
                            if (className.startsWith(packageName)) {
                                minPackageCount--;
                                lastAddedIndex = pkStack.size();
                            }
                        }
                        if (minPackageCount <= 0) {
                            break;
                        }
                    }
                }
            }
            if (lastAddedIndex != -1) {
                int needSize = accepts.size();
                accepts.addAll(pkStack.subList(0, lastAddedIndex));
                if (mEnsureMaxStackCount) {
                    int newSize = accepts.size();
                    int trimStart = accepts.size() - needSize + 1;
                    if (trimStart > 0 && trimStart < accepts.size()) {
                        accepts = accepts.subList(trimStart, newSize);
                    }
                }
            }
        }
        return accepts;
    }

    public String getDescription(Throwable t) {
        return getDescription(null, t);
    }

    public String getDescription(String threadName, Throwable t) {
        String formatResult = null;
        Throwable cause = getLastCause(t);
        if (cause != null) {
            StackTraceElement[] elements = cause.getStackTrace();
            List<StackTraceElement> accepts = Collections.emptyList();
            IReportFilter filter = mStackFilter == null ? DEFAULT_FILTER : mStackFilter;
            if (elements != null && elements.length > 0) {
                accepts = getBestStackTraceElement(elements, filter);
                int size = accepts.size();
                if (size == 0 || accepts.get(0) != elements[0]) {
                    accepts.add(0, elements[0]);
                }
            }
            threadName = threadName == null ? "" : threadName;
            formatResult = filter.formatReportStack(threadName, t, accepts, mShowLineNumber);
            if (TextUtils.isEmpty(formatResult) && filter != DEFAULT_FILTER) {
                formatResult = DEFAULT_FILTER.formatReportStack(threadName, t, accepts, mShowLineNumber);
            }
        }
        return formatResult == null ? "" : formatResult;
    }

    public interface IReportFilter {
        boolean acceptReportStack(String className, String methodName);

        String formatReportStack(String threadName, Throwable cause, List<StackTraceElement> stackTrace, boolean showLineNumber);
    }
}