package com.studioengine.tutor.student;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateStudentCommand {

    String name;
    String email;
    String phone;
}
