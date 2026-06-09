package ru.itis.messaging_engine.plugin.tcp;

import javax.annotation.Nullable;

interface PortMapper {

	@Nullable
	MappingResult map(int port);
}
