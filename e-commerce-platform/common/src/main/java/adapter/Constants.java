package adapter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Constants {
    DLT(".DLT");

    private final String value;
}
