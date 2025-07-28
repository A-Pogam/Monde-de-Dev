import { Component, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import {
  FormBuilder,
  FormGroup,
  Validators,
  ReactiveFormsModule,
} from '@angular/forms';
import { RouterLink } from '@angular/router';
import { SpinLoaderComponent } from '@components/shared/spin-loader/spin-loader.component';
import { ArticleService } from '@core/services/article/article.service';
import {
  ArticleCreationRequest,
  ArticleCreationValues,
} from '@core/types/article.type';
import { Message } from '@core/types/message.type';
import { TopicOptions } from '@core/types/topic.type';
import { WebStorage } from '@lephenix47/webstorage-utility';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-create-article',
  standalone: true,
  imports: [RouterLink, SpinLoaderComponent, ReactiveFormsModule],
  templateUrl: './create-article.component.html',
  styleUrl: './create-article.component.scss',
})
export class CreateArticleComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly articleService = inject(ArticleService);

  public isLoading = toSignal(this.articleService.isLoading$);
  public hasError = toSignal(this.articleService.hasError$);
  public errorMessage = toSignal(this.articleService.errorMessage$);
  public hasSuccess = signal(false);

  public themesArray: TopicOptions[] = [
    { id: 1, theme: 'Python' },
    { id: 2, theme: 'TypeScript' },
    { id: 3, theme: 'Java' },
    { id: 4, theme: 'SEO' },
    { id: 5, theme: 'SASS' },
    { id: 6, theme: 'React' },
  ];

  public creationFormDefaultValues: ArticleCreationValues | null =
    WebStorage.getKey('article-creation');

  public readonly createArticleForm: FormGroup = this.formBuilder.group({
    themeId: [
      this.creationFormDefaultValues?.themeId || '1',
      [Validators.required],
    ],
    description: [
      this.creationFormDefaultValues?.description || '',
      Validators.required,
    ],
    content: ['', Validators.required],
  });

  ngOnInit() {
    if (!this.creationFormDefaultValues) {
      this.resetLocalStorage();
    }
  }

  public onSubmit = (event: Event): void => {
  event.preventDefault();

  const { themeId, description, content } =
    this.createArticleForm.getRawValue();

  const normalizedThemeId = Number(themeId);
  const themeLabel = this.getThemeLabelById(normalizedThemeId); // <== récupérer le thème

  const newArticle: ArticleCreationRequest = {
    title: themeLabel, // <- c’est ici qu’on injecte le nom du thème
    description: description.trim(),
    content: content.trim(),
  };

  const subscription: Subscription = this.articleService
    .postArticle(normalizedThemeId, newArticle)
    .subscribe({
      next: (value: Message) => {
        this.hasSuccess.set(true);
        this.resetForm();
        this.resetLocalStorage();
        subscription.unsubscribe();
      },
      error: (error) => {
        console.error('Erreur lors de la création de l’article :', error);
      }
    });
};

private getThemeLabelById(id: number): string {
  return (
    this.themesArray.find((theme) => theme.id === id)?.theme || 'Thème inconnu'
  );
}


  public setDefaultValueToLocalStorage = (event: Event): void => {
    const element = event.target as HTMLElement;
    const articleCreationValues =
      WebStorage.getKey<ArticleCreationValues>('article-creation') ?? {
        themeId: '1',
        description: '',
      };

    switch (element.tagName.toLowerCase()) {
      case 'select': {
        if ((element as HTMLSelectElement).name === 'topics') {
          articleCreationValues.themeId = (element as HTMLSelectElement).value;
        }
        break;
      }
      case 'textarea': {
        if ((element as HTMLTextAreaElement).name === 'description') {
          articleCreationValues.description = (
            element as HTMLTextAreaElement
          ).value;
        }
        break;
      }
    }

    WebStorage.setKey('article-creation', articleCreationValues);
  };

  private resetForm(): void {
    this.createArticleForm.setValue({
      themeId: '1',
      description: '',
      content: '',
    });
  }

  private resetLocalStorage(): void {
    WebStorage.setKey('article-creation', {
      themeId: '1',
      description: '',
    });
  }
}
