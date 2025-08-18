import { Component, inject, signal } from '@angular/core';
import { Topic } from '@core/types/topic.type';
import { TopicsContainerComponent } from '@components/shared/topics-container/topics-container.component';
import { toSignal } from '@angular/core/rxjs-interop';
import { UserBasicInfo } from '@core/types/user.type';
import { Store } from '@ngrx/store';
import { SpinLoaderComponent } from '@components/shared/spin-loader/spin-loader.component';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { CookiesService } from '@core/services/cookies/cookies.service';
import { UserService } from '@core/services/user/user.service';
import { TopicService } from '@core/services/topic/topic.service';
import { Message } from '@core/types/message.type';
import { Subscription } from 'rxjs';
import { WebStorage } from '@lephenix47/webstorage-utility';

@Component({
  selector: 'app-user',
  standalone: true,
  imports: [TopicsContainerComponent, ReactiveFormsModule, SpinLoaderComponent],
  templateUrl: './user.component.html',
  styleUrl: './user.component.scss',
})
export class UserComponent {
  private readonly router = inject(Router);
  private readonly store = inject(Store);
  private readonly cookiesService = inject(CookiesService);
  private readonly userService = inject(UserService);
  private readonly topicService = inject(TopicService);
  private readonly formBuilder = inject(FormBuilder);

  public userInfo = toSignal<UserBasicInfo>(this.store.select('userInfo'));
  public topicsAreLoading = toSignal<boolean>(this.topicService.isLoading$);
  public topicsHaveAnError = toSignal<boolean>(this.topicService.hasError$);
  public topicsErrorMessage = toSignal<string>(this.topicService.errorMessage$);
  public userUpdateIsLoading = toSignal<boolean>(this.userService.isLoading$);
  public userHasAnError = toSignal<boolean>(this.userService.hasError$);
  public userErrorMessage = toSignal<string>(this.userService.errorMessage$);

  public userSuccessMessage: string = '';
  public topicsArray = signal<Topic[]>([]);

  public readonly userCredentialsForm = this.formBuilder.group({
    username: [this.userInfo()?.username, Validators.required],
    email: [this.userInfo()?.email, [Validators.required, Validators.email]],
    newPassword: ['', [Validators.minLength(8)]], // facultatif, un seul champ
  });

  ngOnInit() {
    this.initializeTopicsArray();
  }

  private initializeTopicsArray = (): void => {
    const subscription: Subscription = this.topicService
      .getAllThemesWithSubscription()
      .subscribe((res: Topic[]) => {
        const subscribedTopics: Topic[] = res.filter((t: Topic) => t.isSubscribed);
        this.topicsArray.update(() => subscribedTopics);
        subscription.unsubscribe();
      });
  };

  onSubmit = (event: Event): void => {
    event.preventDefault();
    this.userSuccessMessage = '';

    const { username, email, newPassword } = this.userCredentialsForm.getRawValue();

    const profilePayload: any = {};
    if (username) profilePayload.username = username.trim();
    if (email) profilePayload.email = email.trim();

    const wantsProfileUpdate = !!(profilePayload.username || profilePayload.email);
    const wantsPasswordChange = !!(newPassword && newPassword.trim().length > 0);

    if (wantsProfileUpdate && wantsPasswordChange) {
      this.userService.updateUser(profilePayload).subscribe(
        () => {
          this.userService.changePassword({ newPassword: newPassword!.trim() }).subscribe(
            (msg: Message) => {
              this.userSuccessMessage = msg.message || 'Password updated. Please log in again.';
              this.logout(); 
            },
            (err) => console.error('Password change failed:', err)
          );
        },
        (err) => console.error('Profile update failed:', err)
      );
      return;
    }

    if (wantsPasswordChange) {
      this.userService.changePassword({ newPassword: newPassword!.trim() }).subscribe(
        (msg: Message) => {
          this.userSuccessMessage = msg.message || 'Password updated. Please log in again.';
          this.logout(); 
        },
        (err) => console.error('Password change failed:', err)
      );
      return;
    }

    if (wantsProfileUpdate) {
      this.userService.updateUser(profilePayload).subscribe(
        (msg: Message) => {
          this.userSuccessMessage = msg.message || 'Successfully updated user profile!';
        },
        (err) => console.error('Profile update failed:', err)
      );
      return;
    }

    this.userSuccessMessage = 'Aucune modification détectée.';
  };

  logout = (): void => {
    this.cookiesService.deleteJwt();
    WebStorage.setKey('article-creation', { themeId: '1', title: '', description: '' });
    this.router.navigate(['/']);
  };

  updateUserThemeSubscription = (id: number) => {
    this.updateTopicsArray(id);
    const subscription: Subscription = this.topicService.unsubscribeToTheme(id).subscribe(() => {
      this.updateTopicsArray(id);
      subscription.unsubscribe();
    });
  };

  private updateTopicsArray = (id: number): void => {
    this.topicsArray.update((topics) => topics.filter((t) => t.id !== id));
  };
}
