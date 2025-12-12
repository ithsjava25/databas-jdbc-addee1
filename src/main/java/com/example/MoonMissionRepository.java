package com.example;

import java.util.List;

public interface MoonMissionRepository {
    List<String> listSpacecraft();
    Mission getMissionById(long id);
    int countByYear(int year);
}
