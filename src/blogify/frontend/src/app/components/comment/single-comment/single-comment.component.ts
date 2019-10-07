import { Component, Input, OnInit } from '@angular/core';
import { Comment } from '../../../models/Comment';
import { AuthService } from '../../../services/auth/auth.service';
import { User } from '../../../models/User';

@Component({
  selector: 'app-single-comment',
  templateUrl: './single-comment.component.html',
  styleUrls: ['./single-comment.component.scss']
})
export class SingleCommentComponent implements OnInit {

    isReady: boolean = false;

    @Input() private comment: Comment;

    private user: User;

    constructor(private userService: AuthService) {}

    async ngOnInit() {
        this.user = await this.userService.fetchUser(this.comment.commenter);
        this.isReady = true;
    }

}
