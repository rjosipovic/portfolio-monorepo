package com.playground.challenge_manager.errors.exceptions.specific;

import com.playground.challenge_manager.errors.exceptions.base.ChallengeManagerException;
import com.playground.challenge_manager.errors.exceptions.enums.ErrorCode;

public class ChallengeSubscriptionException extends ChallengeManagerException {

    public ChallengeSubscriptionException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }

    public ChallengeSubscriptionException(ErrorCode errorCode, String detail, Throwable cause) {
        super(errorCode, detail, cause);
    }
}