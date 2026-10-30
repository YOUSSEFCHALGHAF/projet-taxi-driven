package tn.transporttn.dto.commun;

import java.time.LocalDateTime;

public record PositionResponse(Double latitude, Double longitude, LocalDateTime derniereMiseAJour) {
}