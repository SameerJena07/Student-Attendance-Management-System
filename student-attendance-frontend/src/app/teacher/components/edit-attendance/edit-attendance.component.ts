import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { TeacherService } from '../../../core/services/teacher.service';

@Component({
  selector: 'app-edit-attendance',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './edit-attendance.component.html',
})
export class EditAttendanceComponent implements OnInit {
  courses: any[] = [];
  selectedCourseId: number | null = null;
  editDate: string = '';

  // ✅ Constraint Logic: Allows Today and the next 2 days (duration window)
  minDate: string = '';
  maxDate: string = '';

  // ✅ Precision Logic: Shows the exact second of the 48-hour limit
  maxAllowedMessage: string = '';

  isLoading: boolean = false;
  isSubmitting: boolean = false;

  constructor(private teacherService: TeacherService, private router: Router) {}

  ngOnInit() {
    this.calculateDateLimits();
    this.loadCourses();
  }

  calculateDateLimits() {
    const now = new Date();

    /** * ✅ SELECTION WINDOW LOGIC
     * To allow selection of Jan 8, 9, and 10:
     * minDate is Today (Jan 8)
     * maxDate is Today + 2 days (Jan 10)
     */
    this.minDate = now.toISOString().split('T')[0];

    const futureLimit = new Date();
    futureLimit.setDate(now.getDate() + 2);
    this.maxDate = futureLimit.toISOString().split('T')[0];

    /**
     * ✅ PRECISE DEADLINE MESSAGE
     * Calculates exactly 48 hours from the class start time.
     * Example: Class 8 Jan 6:00:00 PM -> Deadline 10 Jan 6:00:00 PM
     */
    const deadline = new Date(now.getTime() + 48 * 60 * 60 * 1000);

    this.maxAllowedMessage = deadline.toLocaleString('en-IN', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit', // Shows precise seconds
      hour12: true,
    });
  }

  loadCourses() {
    this.isLoading = true;
    this.teacherService.getTeacherCourses().subscribe({
      next: (data) => {
        this.courses = data;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading courses:', error);
        this.isLoading = false;
      },
    });
  }

  proceed() {
    if (this.selectedCourseId && this.editDate) {
      this.isSubmitting = true;

      // Navigate with 'edit' mode to trigger Update logic in the marking component
      setTimeout(() => {
        this.router.navigate(['/teacher/mark-attendance'], {
          queryParams: {
            courseId: this.selectedCourseId,
            date: this.editDate,
            mode: 'edit',
          },
        });
        this.isSubmitting = false;
      }, 500);
    }
  }
}
