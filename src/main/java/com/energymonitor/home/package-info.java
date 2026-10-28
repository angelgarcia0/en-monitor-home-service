/**
 * Home service: the Home bounded context of EnergyMonitor, deployed on its own.
 *
 * <p>Owns the homes: their creation, the access code members join with, the owner and member
 * roles, favourites, ownership transfer, the consumption thresholds and the home type catalogue.
 * The other services ask its internal API whether a user may act on a home and read the
 * thresholds of a home, and learn that a home was deleted from its events.
 *
 * <p>Internal layout: {@code domain}, {@code application}, {@code adapter},
 * {@code infrastructure}.
 */
package com.energymonitor.home;
