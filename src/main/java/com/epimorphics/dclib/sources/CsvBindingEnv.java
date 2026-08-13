package com.epimorphics.dclib.sources;

import com.epimorphics.dclib.framework.BindingEnv;

public class CsvBindingEnv extends BindingEnv {
    private int sourceBytes;

    CsvBindingEnv() {
        super();
    }

    public void setSourceBytes(int sourceBytes) {
        this.sourceBytes = sourceBytes;
    }

    /**
     * The number of bytes in the source data which generates this binding, for progress monitoring purposes.
     * Note - this is an estimate and may not be exact.
     */
    public int sourceBytes() {
        return sourceBytes;
    }
}
