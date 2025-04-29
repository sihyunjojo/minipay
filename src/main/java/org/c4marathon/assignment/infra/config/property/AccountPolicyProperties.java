package org.c4marathon.assignment.infra.config.property;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "business.rule.account")
@Getter
@Setter
public class AccountPolicyProperties {

	@NotNull
	private MainAccountPolicy main;

	@NotNull
	private SavingAccountPolicy saving;
}
