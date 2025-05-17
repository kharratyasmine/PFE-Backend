package com.workpilot.service.GestionRessources.Holiday;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class HolidayServiceImpl implements HolidayService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<Map<String, Object>> getHolidaysByMember(Long memberId) {
        String sql = "SELECT * FROM team_member_holidays WHERE team_member_id = ?";
        return jdbcTemplate.queryForList(sql, memberId);
    }

    @Override
    public boolean checkHolidayExists(Long memberId, LocalDate date) {
        String sql = "SELECT COUNT(*) FROM team_member_holidays WHERE team_member_id = ? AND holiday = ?";
        int count = jdbcTemplate.queryForObject(sql, Integer.class, memberId, date);
        return count > 0;
    }

    @Override
    @Transactional
    public void addSimpleHoliday(Long memberId, LocalDate date) {
        // Vérifier d'abord si l'entrée existe déjà
        if (!checkHolidayExists(memberId, date)) {
            String sql = "INSERT INTO team_member_holidays (team_member_id, holiday) VALUES (?, ?)";
            jdbcTemplate.update(sql, memberId, date);
        }
    }

    @Override
    @Transactional
    public void deleteSimpleHoliday(Long memberId, LocalDate date) {
        String sql = "DELETE FROM team_member_holidays WHERE team_member_id = ? AND holiday = ?";
        jdbcTemplate.update(sql, memberId, date);
    }


}