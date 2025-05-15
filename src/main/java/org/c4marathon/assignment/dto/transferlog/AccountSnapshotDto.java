package org.c4marathon.assignment.dto.transferlog;

import lombok.Builder;
import org.c4marathon.assignment.domain.model.transferlog.AccountSnapshot;

@Builder
public record AccountSnapshotDto(
	Long id,
	String type,
	String number
) {
	public static AccountSnapshotDto from(AccountSnapshot snapshot) {
		if (snapshot == null) return null;
		return new AccountSnapshotDto(
			snapshot.getId(),
			snapshot.getType().name(),
			snapshot.getNumber()
		);
	}
}
