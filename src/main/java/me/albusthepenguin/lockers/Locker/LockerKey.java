package me.albusthepenguin.lockers.Locker;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
@SuppressWarnings("all")
public class LockerKey {
    private final UUID uuid;
    private final int page;
}
