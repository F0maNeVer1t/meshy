package ru.itis.meshy.privategroup.invitation;

import ru.itis.messaging_engine.api.sync.Group.Visibility;

interface State {

	int getValue();

	Visibility getVisibility();

	boolean isAwaitingResponse();
}
