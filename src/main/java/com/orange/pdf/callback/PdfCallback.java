package com.orange.pdf.callback;

import java.util.Map;

public interface Callback<E> {

    void success(E success);

    void error(E error);
}
