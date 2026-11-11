package com.baseplus.core.security;

import java.io.Serializable;

public record JwtAuthenticationDetails(Long sessionId) implements Serializable {
}
