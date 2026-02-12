/*
 * Copyright 2026-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.redis.config;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.SmartLifecycle;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * Creates the necessary {@link RedisMessageListenerContainer} instances for the registered {@link RedisListenerEndpoint
 * endpoints}.
 * <p>
 * Manages the lifecycle of the listener containers.
 *
 * @author Ilyass Bougati
 */
public class RedisListenerEndpointRegistry implements DisposableBean, SmartLifecycle {

	private final List<RedisMessageListenerContainer> containers = new ArrayList<>();
	private final Log logger = LogFactory.getLog(RedisListenerEndpointRegistry.class);
	private boolean running;

	/**
	 * Register a new {@link RedisListenerEndpoint} with the given {@link RedisMessageListenerContainer}.
	 *
	 * @param endpoint the endpoint to register.
	 * @param container the container to register the endpoint with.
	 */
	public void registerListenerContainer(RedisListenerEndpoint endpoint, RedisMessageListenerContainer container) {
		endpoint.setupListenerContainer(container);

		synchronized (this.containers) {
			this.containers.add(container);
		}
	}

	@Override
	public void start() {
		for (RedisMessageListenerContainer container : this.containers) {
			if (!container.isRunning()) {
				container.start();
			}
		}
		this.running = true;
	}

	@Override
	public void stop() {
		for (RedisMessageListenerContainer container : this.containers) {
			if (container.isRunning()) {
				container.stop();
			}
		}
		this.running = false;
	}

	@Override
	public boolean isRunning() {
		return this.running;
	}

	@Override
	public void destroy() throws Exception {
		for (RedisMessageListenerContainer container : this.containers) {
			try {
				container.destroy();
			} catch (Exception ex) {
				logger.error("Error destroying message listener container ", ex);
			}
		}
	}

	public List<RedisMessageListenerContainer> getListenerContainers() {
		return containers;
	}
}
