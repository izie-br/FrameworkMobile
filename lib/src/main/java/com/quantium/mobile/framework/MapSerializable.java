package com.quantium.mobile.framework;

import java.util.Map;

public interface MapSerializable {

    void toMap(Map<String, Object> map);

    Map<String, Object> toMap();

}
