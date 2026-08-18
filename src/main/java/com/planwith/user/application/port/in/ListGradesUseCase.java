package com.planwith.user.application.port.in;

import com.planwith.user.application.dto.GradeCatalogItem;

import java.util.List;

public interface ListGradesUseCase {
    List<GradeCatalogItem> listGrades();
}
