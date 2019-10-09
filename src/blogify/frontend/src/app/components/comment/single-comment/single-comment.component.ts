import { Component, Input, OnInit } from '@angular/core';
import { Comment } from '../../../models/Comment';
import {AuthService} from '../../../services/auth/auth.service';

@Component({
  selector: 'app-single-comment',
  templateUrl: './single-comment.component.html',
  styleUrls: ['./single-comment.component.scss']
})
export class SingleCommentComponent implements OnInit {

    @Input() comment: Comment;

    constructor(private authService: AuthService) {}

    async ngOnInit() {
        // Fetch full user instead of uuid
        this.comment.commenter = await this.authService.fetchUser(<string> this.comment.commenter);
    }

}
