package tn.transporttn.tempsreel;

import com.fasterxml.jackson.annotation.JsonInclude;
import tn.transporttn.dto.commun.PositionResponse;
import tn.transporttn.dto.taxi.ReservationTaxiResponse;

/**
 * Message envoyé sur /user/queue/courses :
 *  { "type": "COURSE",   "courseId": 12, "course": {...} }   statut, chauffeur attribué…
 *  { "type": "POSITION", "courseId": 12, "position": {...} } le chauffeur s'est déplacé
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record EvenementCourse(String type, Long courseId, ReservationTaxiResponse course, PositionResponse position) {

    public static EvenementCourse course(ReservationTaxiResponse course) {
        return new EvenementCourse("COURSE", course.id(), course, null);
    }

    public static EvenementCourse position(Long courseId, PositionResponse position) {
        return new EvenementCourse("POSITION", courseId, null, position);
    }
}