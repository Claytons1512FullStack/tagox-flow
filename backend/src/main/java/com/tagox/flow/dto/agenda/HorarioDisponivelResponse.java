package com.tagox.flow.dto.agenda;

import java.time.LocalTime;

public record HorarioDisponivelResponse(
        LocalTime inicio,
        LocalTime fim
) {
}