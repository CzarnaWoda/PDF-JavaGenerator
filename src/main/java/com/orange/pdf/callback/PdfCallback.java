package com.orange.pdf.callback;

public interface PdfCallback<E> {

    void success(E success);

    void error(E error);
}
