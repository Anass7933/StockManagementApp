package com.stockapp.models.interfaces;

import java.time.OffsetDateTime;

public interface Auditable {
	OffsetDateTime getCreatedAt();
}
