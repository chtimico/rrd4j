package org.rrd4j.graph;

import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.rrd4j.core.Util;
import org.rrd4j.data.DataProcessor;
import org.rrd4j.data.Variable;
import org.rrd4j.data.Variable.Value;

class PrintText extends CommentText {
    static final String UNIT_MARKER = "([^%]?)%(s|S)";
    static final Pattern UNIT_PATTERN = Pattern.compile(UNIT_MARKER);

    private final String srcName;
    private final boolean includedInGraph;
    private final boolean strftime;

    PrintText(String srcName, String text, boolean includedInGraph, boolean strftime) {
        super(text);
        this.srcName = srcName;
        this.includedInGraph = includedInGraph;
        this.strftime = strftime;
    }

    boolean isPrint() {
        return !includedInGraph;
    }

    void resolveText(Locale l, DataProcessor dproc, ValueScaler valueScaler) {
        super.resolveText(l, dproc, valueScaler);
        Value v = dproc.getVariable(srcName);
        if(resolvedText == null) {
            return;
        }
        else if(strftime) {
            if(v != Variable.INVALIDVALUE) {
                long time = v.timestamp;
                resolvedText = String.format(l, resolvedText, new Date(time * 1000));                
            }
            else {
                resolvedText = "-";                
            }
        }
        else {
            double value = v.value;
            Matcher matcher = UNIT_PATTERN.matcher(resolvedText);
            if (matcher.find()) {
                // unit specified
                ValueScaler.Scaled scaled = valueScaler.scale(value, matcher.group(2).equals("s"));
                resolvedText = resolvedText.substring(0, matcher.start()) +
                        matcher.group(1) + scaled.unit + resolvedText.substring(matcher.end());
                value = scaled.value;
            }
            resolvedText = Util.sprintf(l, resolvedText, value);
        }
        trimIfGlue();
    }
}
