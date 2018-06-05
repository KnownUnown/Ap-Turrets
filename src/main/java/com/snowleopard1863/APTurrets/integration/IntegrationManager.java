package com.snowleopard1863.APTurrets.integration;

import java.util.Optional;

public interface IntegrationManager {

	void register(Integration integration);
	<T extends Integration> Optional<T> getIntegration(Class<T> clazz);

}
