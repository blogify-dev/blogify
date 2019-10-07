import { Component, Input, OnInit } from '@angular/core';
import { Comment } from '../../../models/Comment';

@Component({
  selector: 'app-single-comment',
  templateUrl: './single-comment.component.html',
  styleUrls: ['./single-comment.component.scss']
})
export class SingleCommentComponent implements OnInit {

    @Input() comment: Comment;

    constructor() {}

    ngOnInit() {
    }

}
