package org.c4marathon.assignment.dto.log;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
//----MODIFIED PART 1 START----
public class RetryResult<T> {
	private T result;
	private int retryCount;
}
