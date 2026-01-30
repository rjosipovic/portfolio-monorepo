package com.playground.challenge_manager.errors.exceptions.specific;

import com.playground.challenge_manager.errors.exceptions.base.ChallengeManagerException;
import com.playground.challenge_manager.errors.exceptions.enums.ErrorCode;

public class ChallengeDataCorruptedException extends ChallengeManagerException {

    public ChallengeDataCorruptedException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }

    public ChallengeDataCorruptedException(ErrorCode errorCode, String detail, Throwable cause) {
        super(errorCode, detail, cause);
    }
}
