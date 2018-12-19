package com.artezio.arttime.datamodel;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

//TODO rename, because this model represents only non-working days.
@Entity
@Cacheable
@Table(name="\"Day\"")
@XmlAccessorType(XmlAccessType.FIELD)
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
public class Day implements Serializable {

	private static final long serialVersionUID = 902134612376410L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@NotNull
	@XmlElement
	@XmlSchemaType(name = "date")
	@Column(name="\"date\"")
	@Temporal(value = TemporalType.DATE)
	private Date date;
    @XmlElement(required=true)
	private Boolean working;
    @XmlElement(required=true)
	private Boolean holiday = false;
	@XmlElement
	@XmlSchemaType(name = "date")
	@Temporal(value = TemporalType.DATE)
	private Date shiftedFrom;
	@XmlElement
	@XmlSchemaType(name = "date")
	@Temporal(value = TemporalType.DATE)
	private Date shiftedTo;
	@Size(max = 255)
	@Column(name="\"comment\"")
	private String comment;
	@OneToOne
	@XmlTransient
	private WorkdaysCalendar workdaysCalendar;

	public static final Comparator<Day> DATE_COMPARATOR = (d1, d2) -> d1.getDate().compareTo(d2.getDate());

	public Day() {

	}

	public Day(Date date, WorkdaysCalendar workdaysCalendar) {
		this.date = date;
		this.workdaysCalendar = workdaysCalendar;
		this.working = !isWeekend();

	}

	public Day(Date date, WorkdaysCalendar workdaysCalendar, Boolean isWorking) {
		this.date = date;
		this.workdaysCalendar = workdaysCalendar;
		this.working = isWorking;
	}

    /**
     * Required due to old database issue, when null values were possible
     * Null values cause IntegrationFacade XML errors
     */
    @PostLoad
    private void ensureFieldsNotNull() {
        if (working == null) {
            working = !isWeekend();
        }
        if (holiday == null) {
            holiday = false;
        }
    }

	public void switchDayType() {
		if (isWorking()) {
			working = false;
			holiday = false;
		} else if (isHoliday()) {
			holiday = false;
			working = true;
		} else {
			holiday = true;
			working = false;
		}
	}

	protected Boolean isWeekend() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
		if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
			return true;
		}
		return false;
	}

	public Long getId() {
		return id;
	}

	public Boolean isWorking() {
		return working;
	}

	public void setWorking(Boolean working) {
		this.working = working;
	}

	public Date getDate() {
		return date;
	}

	public Boolean isHoliday() {
		return holiday;
	}

	public void setHoliday(Boolean holiday) {
		this.holiday = holiday;
		if (holiday != null && holiday) {
            working = false;
        }
	}

	public WorkdaysCalendar getWorkdaysCalendar() {
		return workdaysCalendar;
	}

	public void setWorkdaysCalendar(WorkdaysCalendar workdaysCalendar) {
		this.workdaysCalendar = workdaysCalendar;
	}

	public Date getShiftedFrom() {
		return shiftedFrom;
	}

	public void setShiftedFrom(Date shiftedFrom) {
		this.shiftedFrom = shiftedFrom;
	}

	public Date getShiftedTo() {
		return shiftedTo;
	}

	public void setShiftedTo(Date shiftedTo) {
		this.shiftedTo = shiftedTo;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Day))
			return false;

		Day day = (Day) o;

		if (id != null ? !id.equals(day.id) : day.id != null)
			return false;

		return true;
	}

	@Override
	///CLOVER:OFF
	public int hashCode() {
		return id != null ? id.hashCode() : 0;
	}
	///CLOVER:ON

	@Override
	public String toString() {
		return MessageFormat.format("{0} '{'date={1, date, short}, isWorking={2}'}'",
				new Object[] { getClass().getSimpleName(), date, working });

	}

}
