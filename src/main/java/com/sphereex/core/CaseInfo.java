package com.sphereex.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

// Case meta data
@Getter
@RequiredArgsConstructor
public final class CaseInfo {

    private final String name;

//    feature which case belong to
    private final String feature;

//    case tag
    private final String tag;

//    describe of case
    private final String message;
}
