(window["webpackJsonp"] = window["webpackJsonp"] || []).push([["main"],{

/***/ "./$$_lazy_route_resource lazy recursive":
/*!******************************************************!*\
  !*** ./$$_lazy_route_resource lazy namespace object ***!
  \******************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

function webpackEmptyAsyncContext(req) {
	// Here Promise.resolve().then() is used instead of new Promise() to prevent
	// uncaught exception popping up in devtools
	return Promise.resolve().then(function() {
		var e = new Error("Cannot find module '" + req + "'");
		e.code = 'MODULE_NOT_FOUND';
		throw e;
	});
}
webpackEmptyAsyncContext.keys = function() { return []; };
webpackEmptyAsyncContext.resolve = webpackEmptyAsyncContext;
module.exports = webpackEmptyAsyncContext;
webpackEmptyAsyncContext.id = "./$$_lazy_route_resource lazy recursive";

/***/ }),

/***/ "./node_modules/raw-loader/index.js!./src/app/app.component.html":
/*!**************************************************************!*\
  !*** ./node_modules/raw-loader!./src/app/app.component.html ***!
  \**************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = "<app-navbar></app-navbar>\n<router-outlet></router-outlet>\n<div id=\"license-info\">\n    <span id=\"version\">blogify-core PRX2</span>\n    <span id=\"license\">Copyright &copy;2019 the Blogify contributors</span>\n    <span id=\"gpl\">Licensed under the GNU General Public License Version 3 (GPLv3)</span>\n    <span id=\"source\"><a href=\"https://github.com/blogify-dev/blogify\">Source code</a></span>\n</div>\n"

/***/ }),

/***/ "./node_modules/raw-loader/index.js!./src/app/components/comment/comment.component.html":
/*!*************************************************************************************!*\
  !*** ./node_modules/raw-loader!./src/app/components/comment/comment.component.html ***!
  \*************************************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = "<p>comment works!</p>\n\n<div *ngIf=\"comments\">\n    <div *ngFor=\"let comment of comments\">\n        <p>{{comment.content}}</p>\n        <p>uuid: {{comment.uuid}}</p>\n    </div>\n</div>\n"

/***/ }),

/***/ "./node_modules/raw-loader/index.js!./src/app/components/home/home.component.html":
/*!*******************************************************************************!*\
  !*** ./node_modules/raw-loader!./src/app/components/home/home.component.html ***!
  \*******************************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = "<app-show-all-articles></app-show-all-articles>\n"

/***/ }),

/***/ "./node_modules/raw-loader/index.js!./src/app/components/login/login.component.html":
/*!*********************************************************************************!*\
  !*** ./node_modules/raw-loader!./src/app/components/login/login.component.html ***!
  \*********************************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = "<p>login works!</p>\n\n<div>\n\n  <span>Username</span>: <input type=\"text\" name=\"user\" [(ngModel)]=\"loginCredentials.username\"><br>\n  <span>Password</span>: <input type=\"text\" name=\"pass\" [(ngModel)]=\"loginCredentials.password\"><br>\n\n</div>\n\n<button (click)=\"login()\">Login</button>\n\n<div>loginCredentials: {{loginCredentials | json}}</div>\n\n<div>user: {{user | json}}</div>\n\n<div *ngIf=\"user\">\n    <a routerLink=\"/home\">Home</a> <br>\n    <a routerLink=\"/profile/{{user.uuid}}\">Profile</a>\n</div>\n<br>\n"

/***/ }),

/***/ "./node_modules/raw-loader/index.js!./src/app/components/navbar/navbar.component.html":
/*!***********************************************************************************!*\
  !*** ./node_modules/raw-loader!./src/app/components/navbar/navbar.component.html ***!
  \***********************************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = "<nav class=\"navbar\">\n\n    <div id=\"navbar-list\">\n\n        <span class=\"navbar-link\" id=\"home\">\n            <a routerLink=\"home\">\n                <img height=\"40px\"\n                     src=\"assets/images/logo.svg\"\n                     alt=\"Blogify\" />\n            </a>\n        </span>\n\n        <span class=\"navbar-link\" id=\"login\">\n            <a routerLink=\"/login\" *ngIf=\"authService.userToken == ''\">Login</a>\n            <a (click)=\"navigateToProfile()\" *ngIf=\"authService.userToken != ''\">Profile</a>\n        </span>\n\n        <span class=\"navbar-link\" id=\"register\" *ngIf=\"authService.userToken == ''\">\n            <a routerLink=\"/register\">Register</a>\n        </span>\n\n    </div>\n\n</nav>\n"

/***/ }),

/***/ "./node_modules/raw-loader/index.js!./src/app/components/newarticle/new-article.component.html":
/*!********************************************************************************************!*\
  !*** ./node_modules/raw-loader!./src/app/components/newarticle/new-article.component.html ***!
  \********************************************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = "<p>new-article works!</p>\n\nTitle: <input [(ngModel)]=\"article.title\">\nText: <input [(ngModel)]=\"article.content.text\">\nSummary:<input [(ngModel)]=\"article.content.summary\">\n<button (click)=\"createNewArticle()\">Create New Article</button>\n\n<div>article: {{article | json}}</div>\n"

/***/ }),

/***/ "./node_modules/raw-loader/index.js!./src/app/components/profile/profile.component.html":
/*!*************************************************************************************!*\
  !*** ./node_modules/raw-loader!./src/app/components/profile/profile.component.html ***!
  \*************************************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = "<p>profile works!</p>\n\n<span>{{user | json}}</span>\n"

/***/ }),

/***/ "./node_modules/raw-loader/index.js!./src/app/components/register/register.component.html":
/*!***************************************************************************************!*\
  !*** ./node_modules/raw-loader!./src/app/components/register/register.component.html ***!
  \***************************************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = "<p>register works!</p>\nNew Username: <input type=\"text\" [(ngModel)]=\"user.username\"><br>\nNew Password: <input type=\"text\" [(ngModel)]=\"user.password\"><br>\nName: <input type=\"text\" [(ngModel)]=\"user.name\"><br>\nEmail: <input type=\"text\" [(ngModel)]=\"user.email\"><br>\n<button (click)=\"register()\">Submit</button>\n"

/***/ }),

/***/ "./node_modules/raw-loader/index.js!./src/app/components/search/search.component.html":
/*!***********************************************************************************!*\
  !*** ./node_modules/raw-loader!./src/app/components/search/search.component.html ***!
  \***********************************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = "<p>search works!</p>\n"

/***/ }),

/***/ "./node_modules/raw-loader/index.js!./src/app/components/show-all-articles/show-all-articles.component.html":
/*!*********************************************************************************************************!*\
  !*** ./node_modules/raw-loader!./src/app/components/show-all-articles/show-all-articles.component.html ***!
  \*********************************************************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = "<section class=\"main\">\n\n    <section class=\"articles\">\n\n        <div id=\"articles-header\">\n            <h2>Articles</h2>\n\n            <a id=\"header-create-btn\" (click)=\"navigateToNewArticle()\">+</a>\n        </div>\n\n        <div class=\"articles-container\">\n\n            <div class=\"article\" *ngFor=\"let article of articles\">\n\n                <div class=\"article-first-line\">\n                    <h1 class=\"article-title\">{{article.title}}</h1>\n                    <h4 class=\"article-author\">Posted by  {{article.createdBy.username}} </h4>\n                </div>\n\n                <span class=\"article-summary\">{{article.content.summary}}</span>\n\n\n                <h3 class=\"read-more\">\n                    <a class=\"read-more\" routerLink=\"/article/{{article.uuid}}\">Read More</a>\n                </h3>\n\n                <!-- TODO: Actually impalement this-->\n                <h4 class=\"article-comments-count\">Comments: 4</h4>\n\n                <div class=\"article-tags\" *ngIf=\"article.categories.length > 0\">\n                    <span class=\"tags-title\">Tags:&nbsp;</span>\n                    <span *ngFor=\"let tag of article.categories\">{{tag.name}},&nbsp;</span>\n                </div>\n\n                <span class=\"article-no-tags\" *ngIf=\"article.categories.length == 0\">\n                    <em>No tags</em>\n                </span>\n\n            </div>\n        </div>\n\n    </section>\n\n</section>\n"

/***/ }),

/***/ "./node_modules/raw-loader/index.js!./src/app/components/show-article/show-article.component.html":
/*!***********************************************************************************************!*\
  !*** ./node_modules/raw-loader!./src/app/components/show-article/show-article.component.html ***!
  \***********************************************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = "<p>show-article works!</p>\n<div *ngIf=\"article && articleContent && articleAuthor\">\n    <h3>{{article.title}}</h3>\n    <p>Categories</p>\n    <ul>\n        <li *ngFor=\"let cat of article.categories\">{{cat.name}}</li>\n    </ul>\n    <p>{{articleContent.text}}</p>\n    <p>{{articleContent.summary}}</p>\n    <p>UUID: {{article.uuid}}</p>\n    <p>Created at: {{convertTimeStampToHumanDate(article.createdAt)}}</p>\n    <p>Created by {{articleAuthor.username}}</p>\n\n    <h4>comments:</h4>\n    <app-comment [articleUUID]=\"article.uuid\"></app-comment>\n</div>\n"

/***/ }),

/***/ "./src/app/app-routing.module.ts":
/*!***************************************!*\
  !*** ./src/app/app-routing.module.ts ***!
  \***************************************/
/*! exports provided: AppRoutingModule */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "AppRoutingModule", function() { return AppRoutingModule; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "./node_modules/tslib/tslib.es6.js");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm5/core.js");
/* harmony import */ var _angular_router__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @angular/router */ "./node_modules/@angular/router/fesm5/router.js");
/* harmony import */ var _components_home_home_component__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ./components/home/home.component */ "./src/app/components/home/home.component.ts");
/* harmony import */ var _components_login_login_component__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! ./components/login/login.component */ "./src/app/components/login/login.component.ts");
/* harmony import */ var _components_register_register_component__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! ./components/register/register.component */ "./src/app/components/register/register.component.ts");
/* harmony import */ var _components_profile_profile_component__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! ./components/profile/profile.component */ "./src/app/components/profile/profile.component.ts");
/* harmony import */ var _components_search_search_component__WEBPACK_IMPORTED_MODULE_7__ = __webpack_require__(/*! ./components/search/search.component */ "./src/app/components/search/search.component.ts");
/* harmony import */ var _components_newarticle_new_article_component__WEBPACK_IMPORTED_MODULE_8__ = __webpack_require__(/*! ./components/newarticle/new-article.component */ "./src/app/components/newarticle/new-article.component.ts");
/* harmony import */ var _components_show_article_show_article_component__WEBPACK_IMPORTED_MODULE_9__ = __webpack_require__(/*! ./components/show-article/show-article.component */ "./src/app/components/show-article/show-article.component.ts");










var routes = [
    { path: 'home', component: _components_home_home_component__WEBPACK_IMPORTED_MODULE_3__["HomeComponent"] },
    { path: '', redirectTo: '/home', pathMatch: 'full' },
    { path: 'login', component: _components_login_login_component__WEBPACK_IMPORTED_MODULE_4__["LoginComponent"] },
    { path: 'register', component: _components_register_register_component__WEBPACK_IMPORTED_MODULE_5__["RegisterComponent"] },
    { path: 'search', component: _components_search_search_component__WEBPACK_IMPORTED_MODULE_7__["SearchComponent"] },
    { path: 'new-article', component: _components_newarticle_new_article_component__WEBPACK_IMPORTED_MODULE_8__["NewArticleComponent"] },
    { path: 'profile/:uuid', component: _components_profile_profile_component__WEBPACK_IMPORTED_MODULE_6__["ProfileComponent"] },
    { path: 'article/:uuid', component: _components_show_article_show_article_component__WEBPACK_IMPORTED_MODULE_9__["ShowArticleComponent"] },
];
var AppRoutingModule = /** @class */ (function () {
    function AppRoutingModule() {
    }
    AppRoutingModule = tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
        Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["NgModule"])({
            imports: [_angular_router__WEBPACK_IMPORTED_MODULE_2__["RouterModule"].forRoot(routes)],
            exports: [_angular_router__WEBPACK_IMPORTED_MODULE_2__["RouterModule"]]
        })
    ], AppRoutingModule);
    return AppRoutingModule;
}());



/***/ }),

/***/ "./src/app/app.component.scss":
/*!************************************!*\
  !*** ./src/app/app.component.scss ***!
  \************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = "a + a {\n  margin-left: 10px;\n}\n\n#license-info {\n  display: flex;\n  flex-direction: column;\n  justify-content: space-evenly;\n  align-items: center;\n}\n/*# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi9ob21lL2x1Y3lhZ2FtYWl0ZS9ibG9naWZ5L3NyYy9ibG9naWZ5L2Zyb250ZW5kL3NyYy9hcHAvYXBwLmNvbXBvbmVudC5zY3NzIiwic3JjL2FwcC9hcHAuY29tcG9uZW50LnNjc3MiXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IkFBQUE7RUFDRSxpQkFBQTtBQ0NGOztBREVBO0VBQ0ksYUFBQTtFQUNBLHNCQUFBO0VBQ0EsNkJBQUE7RUFDQSxtQkFBQTtBQ0NKIiwiZmlsZSI6InNyYy9hcHAvYXBwLmNvbXBvbmVudC5zY3NzIiwic291cmNlc0NvbnRlbnQiOlsiYSthIHtcbiAgbWFyZ2luLWxlZnQ6IDEwcHg7XG59XG5cbiNsaWNlbnNlLWluZm8ge1xuICAgIGRpc3BsYXk6ICAgICAgICAgICAgICAgICBmbGV4O1xuICAgIGZsZXgtZGlyZWN0aW9uOiAgICAgICAgY29sdW1uO1xuICAgIGp1c3RpZnktY29udGVudDogc3BhY2UtZXZlbmx5O1xuICAgIGFsaWduLWl0ZW1zOiAgICAgICAgICAgY2VudGVyO1xufVxuIiwiYSArIGEge1xuICBtYXJnaW4tbGVmdDogMTBweDtcbn1cblxuI2xpY2Vuc2UtaW5mbyB7XG4gIGRpc3BsYXk6IGZsZXg7XG4gIGZsZXgtZGlyZWN0aW9uOiBjb2x1bW47XG4gIGp1c3RpZnktY29udGVudDogc3BhY2UtZXZlbmx5O1xuICBhbGlnbi1pdGVtczogY2VudGVyO1xufSJdfQ== */"

/***/ }),

/***/ "./src/app/app.component.ts":
/*!**********************************!*\
  !*** ./src/app/app.component.ts ***!
  \**********************************/
/*! exports provided: AppComponent */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "AppComponent", function() { return AppComponent; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "./node_modules/tslib/tslib.es6.js");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm5/core.js");


var AppComponent = /** @class */ (function () {
    function AppComponent() {
    }
    AppComponent = tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
        Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Component"])({
            selector: 'app-root',
            template: __webpack_require__(/*! raw-loader!./app.component.html */ "./node_modules/raw-loader/index.js!./src/app/app.component.html"),
            styles: [__webpack_require__(/*! ./app.component.scss */ "./src/app/app.component.scss")]
        })
    ], AppComponent);
    return AppComponent;
}());



/***/ }),

/***/ "./src/app/app.module.ts":
/*!*******************************!*\
  !*** ./src/app/app.module.ts ***!
  \*******************************/
/*! exports provided: AppModule */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "AppModule", function() { return AppModule; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "./node_modules/tslib/tslib.es6.js");
/* harmony import */ var _angular_platform_browser__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/platform-browser */ "./node_modules/@angular/platform-browser/fesm5/platform-browser.js");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm5/core.js");
/* harmony import */ var _angular_common_http__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! @angular/common/http */ "./node_modules/@angular/common/fesm5/http.js");
/* harmony import */ var _angular_forms__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! @angular/forms */ "./node_modules/@angular/forms/fesm5/forms.js");
/* harmony import */ var _app_routing_module__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! ./app-routing.module */ "./src/app/app-routing.module.ts");
/* harmony import */ var _app_component__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! ./app.component */ "./src/app/app.component.ts");
/* harmony import */ var _components_login_login_component__WEBPACK_IMPORTED_MODULE_7__ = __webpack_require__(/*! ./components/login/login.component */ "./src/app/components/login/login.component.ts");
/* harmony import */ var _components_register_register_component__WEBPACK_IMPORTED_MODULE_8__ = __webpack_require__(/*! ./components/register/register.component */ "./src/app/components/register/register.component.ts");
/* harmony import */ var _components_profile_profile_component__WEBPACK_IMPORTED_MODULE_9__ = __webpack_require__(/*! ./components/profile/profile.component */ "./src/app/components/profile/profile.component.ts");
/* harmony import */ var _components_home_home_component__WEBPACK_IMPORTED_MODULE_10__ = __webpack_require__(/*! ./components/home/home.component */ "./src/app/components/home/home.component.ts");
/* harmony import */ var _angular_router__WEBPACK_IMPORTED_MODULE_11__ = __webpack_require__(/*! @angular/router */ "./node_modules/@angular/router/fesm5/router.js");
/* harmony import */ var _components_newarticle_new_article_component__WEBPACK_IMPORTED_MODULE_12__ = __webpack_require__(/*! ./components/newarticle/new-article.component */ "./src/app/components/newarticle/new-article.component.ts");
/* harmony import */ var _components_search_search_component__WEBPACK_IMPORTED_MODULE_13__ = __webpack_require__(/*! ./components/search/search.component */ "./src/app/components/search/search.component.ts");
/* harmony import */ var _components_show_article_show_article_component__WEBPACK_IMPORTED_MODULE_14__ = __webpack_require__(/*! ./components/show-article/show-article.component */ "./src/app/components/show-article/show-article.component.ts");
/* harmony import */ var _components_show_all_articles_show_all_articles_component__WEBPACK_IMPORTED_MODULE_15__ = __webpack_require__(/*! ./components/show-all-articles/show-all-articles.component */ "./src/app/components/show-all-articles/show-all-articles.component.ts");
/* harmony import */ var _components_comment_comment_component__WEBPACK_IMPORTED_MODULE_16__ = __webpack_require__(/*! ./components/comment/comment.component */ "./src/app/components/comment/comment.component.ts");
/* harmony import */ var _components_navbar_navbar_component__WEBPACK_IMPORTED_MODULE_17__ = __webpack_require__(/*! ./components/navbar/navbar.component */ "./src/app/components/navbar/navbar.component.ts");


















var AppModule = /** @class */ (function () {
    function AppModule() {
    }
    AppModule = tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
        Object(_angular_core__WEBPACK_IMPORTED_MODULE_2__["NgModule"])({
            declarations: [
                _app_component__WEBPACK_IMPORTED_MODULE_6__["AppComponent"],
                _components_login_login_component__WEBPACK_IMPORTED_MODULE_7__["LoginComponent"],
                _components_register_register_component__WEBPACK_IMPORTED_MODULE_8__["RegisterComponent"],
                _components_profile_profile_component__WEBPACK_IMPORTED_MODULE_9__["ProfileComponent"],
                _components_home_home_component__WEBPACK_IMPORTED_MODULE_10__["HomeComponent"],
                _components_newarticle_new_article_component__WEBPACK_IMPORTED_MODULE_12__["NewArticleComponent"],
                _components_search_search_component__WEBPACK_IMPORTED_MODULE_13__["SearchComponent"],
                _components_show_article_show_article_component__WEBPACK_IMPORTED_MODULE_14__["ShowArticleComponent"],
                _components_show_all_articles_show_all_articles_component__WEBPACK_IMPORTED_MODULE_15__["ShowAllArticlesComponent"],
                _components_comment_comment_component__WEBPACK_IMPORTED_MODULE_16__["CommentComponent"],
                _components_navbar_navbar_component__WEBPACK_IMPORTED_MODULE_17__["NavbarComponent"]
            ],
            imports: [
                _angular_platform_browser__WEBPACK_IMPORTED_MODULE_1__["BrowserModule"],
                _angular_router__WEBPACK_IMPORTED_MODULE_11__["RouterModule"],
                _app_routing_module__WEBPACK_IMPORTED_MODULE_5__["AppRoutingModule"],
                _angular_common_http__WEBPACK_IMPORTED_MODULE_3__["HttpClientModule"],
                _angular_forms__WEBPACK_IMPORTED_MODULE_4__["FormsModule"],
            ],
            providers: [],
            bootstrap: [_app_component__WEBPACK_IMPORTED_MODULE_6__["AppComponent"]]
        })
    ], AppModule);
    return AppModule;
}());



/***/ }),

/***/ "./src/app/components/comment/comment.component.scss":
/*!***********************************************************!*\
  !*** ./src/app/components/comment/comment.component.scss ***!
  \***********************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = "\n/*# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IiIsImZpbGUiOiJzcmMvYXBwL2NvbXBvbmVudHMvY29tbWVudC9jb21tZW50LmNvbXBvbmVudC5zY3NzIn0= */"

/***/ }),

/***/ "./src/app/components/comment/comment.component.ts":
/*!*********************************************************!*\
  !*** ./src/app/components/comment/comment.component.ts ***!
  \*********************************************************/
/*! exports provided: CommentComponent */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "CommentComponent", function() { return CommentComponent; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "./node_modules/tslib/tslib.es6.js");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm5/core.js");
/* harmony import */ var _services_comments_comments_service__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ../../services/comments/comments.service */ "./src/app/services/comments/comments.service.ts");



var CommentComponent = /** @class */ (function () {
    function CommentComponent(commentService) {
        this.commentService = commentService;
    }
    CommentComponent.prototype.ngOnInit = function () {
        var _this = this;
        this.commentService.getCommentsForArticle(this.articleUUID).toPromise().then(function (it) {
            _this.comments = it;
        });
    };
    CommentComponent.ctorParameters = function () { return [
        { type: _services_comments_comments_service__WEBPACK_IMPORTED_MODULE_2__["CommentsService"] }
    ]; };
    tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
        Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Input"])()
    ], CommentComponent.prototype, "articleUUID", void 0);
    CommentComponent = tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
        Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Component"])({
            selector: 'app-comment',
            template: __webpack_require__(/*! raw-loader!./comment.component.html */ "./node_modules/raw-loader/index.js!./src/app/components/comment/comment.component.html"),
            styles: [__webpack_require__(/*! ./comment.component.scss */ "./src/app/components/comment/comment.component.scss")]
        })
    ], CommentComponent);
    return CommentComponent;
}());



/***/ }),

/***/ "./src/app/components/home/home.component.scss":
/*!*****************************************************!*\
  !*** ./src/app/components/home/home.component.scss ***!
  \*****************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = "\n/*# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IiIsImZpbGUiOiJzcmMvYXBwL2NvbXBvbmVudHMvaG9tZS9ob21lLmNvbXBvbmVudC5zY3NzIn0= */"

/***/ }),

/***/ "./src/app/components/home/home.component.ts":
/*!***************************************************!*\
  !*** ./src/app/components/home/home.component.ts ***!
  \***************************************************/
/*! exports provided: HomeComponent */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "HomeComponent", function() { return HomeComponent; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "./node_modules/tslib/tslib.es6.js");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm5/core.js");


var HomeComponent = /** @class */ (function () {
    function HomeComponent() {
        this.title = 'blogify';
    }
    HomeComponent.prototype.ngOnInit = function () {
    };
    HomeComponent = tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
        Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Component"])({
            selector: 'app-home',
            template: __webpack_require__(/*! raw-loader!./home.component.html */ "./node_modules/raw-loader/index.js!./src/app/components/home/home.component.html"),
            styles: [__webpack_require__(/*! ./home.component.scss */ "./src/app/components/home/home.component.scss")]
        })
    ], HomeComponent);
    return HomeComponent;
}());



/***/ }),

/***/ "./src/app/components/login/login.component.scss":
/*!*******************************************************!*\
  !*** ./src/app/components/login/login.component.scss ***!
  \*******************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = "\n/*# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IiIsImZpbGUiOiJzcmMvYXBwL2NvbXBvbmVudHMvbG9naW4vbG9naW4uY29tcG9uZW50LnNjc3MifQ== */"

/***/ }),

/***/ "./src/app/components/login/login.component.ts":
/*!*****************************************************!*\
  !*** ./src/app/components/login/login.component.ts ***!
  \*****************************************************/
/*! exports provided: LoginComponent */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "LoginComponent", function() { return LoginComponent; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "./node_modules/tslib/tslib.es6.js");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm5/core.js");
/* harmony import */ var _services_auth_auth_service__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ../../services/auth/auth.service */ "./src/app/services/auth/auth.service.ts");



var LoginComponent = /** @class */ (function () {
    function LoginComponent(authService) {
        this.authService = authService;
        this.loginCredentials = { username: '', password: '' };
        this.ngOnInit();
    }
    LoginComponent.prototype.ngOnInit = function () {
    };
    LoginComponent.prototype.login = function () {
        return tslib__WEBPACK_IMPORTED_MODULE_0__["__awaiter"](this, void 0, void 0, function () {
            var token, uuid, _a;
            return tslib__WEBPACK_IMPORTED_MODULE_0__["__generator"](this, function (_b) {
                switch (_b.label) {
                    case 0: return [4 /*yield*/, this.authService.login(this.loginCredentials)];
                    case 1:
                        token = _b.sent();
                        console.log(token);
                        return [4 /*yield*/, this.authService.getUserUUID(token.token)];
                    case 2:
                        uuid = _b.sent();
                        _a = this;
                        return [4 /*yield*/, this.authService.getUser(uuid.uuid)];
                    case 3:
                        _a.user = _b.sent();
                        console.log(this.user);
                        console.log(this.loginCredentials);
                        console.log(this.authService.userToken);
                        return [2 /*return*/];
                }
            });
        });
    };
    LoginComponent.ctorParameters = function () { return [
        { type: _services_auth_auth_service__WEBPACK_IMPORTED_MODULE_2__["AuthService"] }
    ]; };
    LoginComponent = tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
        Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Component"])({
            selector: 'app-login',
            template: __webpack_require__(/*! raw-loader!./login.component.html */ "./node_modules/raw-loader/index.js!./src/app/components/login/login.component.html"),
            styles: [__webpack_require__(/*! ./login.component.scss */ "./src/app/components/login/login.component.scss")]
        })
    ], LoginComponent);
    return LoginComponent;
}());



/***/ }),

/***/ "./src/app/components/navbar/navbar.component.scss":
/*!*********************************************************!*\
  !*** ./src/app/components/navbar/navbar.component.scss ***!
  \*********************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = "@import url(\"https://fonts.googleapis.com/css?family=Lexend+Deca|Nunito+Sans&display=swap\");\nhtml {\n  --body-fg: rgb(45, 45, 45);\n  --body-bg: rgb(252, 252, 252);\n  --header-fg: rgb(45, 45, 45);\n  --header-bg: rgb(245, 245, 245);\n  --card-fg: rgb(70, 70, 70);\n  --card-bg: rgb(250, 250, 250);\n}\nhtml[data-theme=dark] {\n  --body-fg: rgb(252, 252, 252);\n  --body-bg: rgb(45, 45, 45);\n  --header-fg: rgb(245, 245, 245);\n  --header-bg: rgb(50, 50, 50);\n  --card-fg: rgb(250, 250, 250);\n  --card-bg: rgb(70, 70, 70);\n}\n#navbar-list {\n  display: flex;\n  flex-direction: row;\n  justify-content: center;\n  align-items: center;\n  padding: 2em 3em;\n  color: var(--header-fg);\n  background-color: var(--header-bg);\n  box-shadow: 0 0 6px 1px rgba(0, 0, 0, 0.2);\n}\n#navbar-list .navbar-link {\n  padding-left: 2.75em;\n  font-size: 1.25em;\n}\n#navbar-list .navbar-link#home {\n  margin-right: auto;\n  padding-right: 15px;\n  padding-left: 0;\n}\n#navbar-list .navbar-link:not(#home) a {\n  color: var(--header-fg);\n  padding-bottom: 0.35em;\n  border-bottom: 1px solid var(--header-fg);\n}\n/*# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi9ob21lL2x1Y3lhZ2FtYWl0ZS9ibG9naWZ5L3NyYy9ibG9naWZ5L2Zyb250ZW5kL3NyYy9hcHAvY29tcG9uZW50cy9uYXZiYXIvbmF2YmFyLmNvbXBvbmVudC5zY3NzIiwiL2hvbWUvbHVjeWFnYW1haXRlL2Jsb2dpZnkvc3JjL2Jsb2dpZnkvZnJvbnRlbmQvc3JjL3N0eWxlcy9jb2xvdXJzLnNjc3MiLCJzcmMvYXBwL2NvbXBvbmVudHMvbmF2YmFyL25hdmJhci5jb21wb25lbnQuc2NzcyIsIi9ob21lL2x1Y3lhZ2FtYWl0ZS9ibG9naWZ5L3NyYy9ibG9naWZ5L2Zyb250ZW5kL3N0ZGluIl0sIm5hbWVzIjpbXSwibWFwcGluZ3MiOiJBQUFRLDJGQUFBO0FDQVI7RUFFSSwwQkFBQTtFQUNBLDZCQUFBO0VBRUEsNEJBQUE7RUFDQSwrQkFBQTtFQUVBLDBCQUFBO0VBQ0EsNkJBQUE7QUNESjtBREdJO0VBQ0ksNkJBQUE7RUFDQSwwQkFBQTtFQUVBLCtCQUFBO0VBQ0EsNEJBQUE7RUFFQSw2QkFBQTtFQUNBLDBCQUFBO0FDSFI7QUNiQTtFQUVJLGFBQUE7RUFDQSxtQkFBQTtFQUNBLHVCQUFBO0VBQ0EsbUJBQUE7RUFFQSxnQkFBQTtFQUVBLHVCQUFBO0VBQ0Esa0NBQUE7RUFLUSwwQ0FISztBRGNqQjtBQ1RJO0VBRUksb0JBQUE7RUFFQSxpQkFBQTtBRFNSO0FDUFE7RUFDSSxrQkFBQTtFQUNBLG1CQUFBO0VBQ0EsZUFBQTtBRFNaO0FDTlE7RUFDSSx1QkFBQTtFQUVBLHNCQUFBO0VBQ0EseUNBQUE7QURPWiIsImZpbGUiOiJzcmMvYXBwL2NvbXBvbmVudHMvbmF2YmFyL25hdmJhci5jb21wb25lbnQuc2NzcyIsInNvdXJjZXNDb250ZW50IjpbIkBpbXBvcnQgdXJsKCdodHRwczovL2ZvbnRzLmdvb2dsZWFwaXMuY29tL2Nzcz9mYW1pbHk9TGV4ZW5kK0RlY2F8TnVuaXRvK1NhbnMmZGlzcGxheT1zd2FwJyk7XG5cbiRsZXhlbmRhOiAnTGV4ZW5kIERlY2EnLCBzYW5zLXNlcmlmO1xuJG51bml0bzogJ051bml0byBTYW5zJywgc2Fucy1zZXJpZjtcbiIsImh0bWwge1xuXG4gICAgLS1ib2R5LWZnOiByZ2IoNDUsIDQ1LCA0NSk7XG4gICAgLS1ib2R5LWJnOiByZ2IoMjUyLCAyNTIsIDI1Mik7XG5cbiAgICAtLWhlYWRlci1mZzogcmdiKDQ1LCA0NSwgNDUpO1xuICAgIC0taGVhZGVyLWJnOiByZ2IoMjQ1LCAyNDUsIDI0NSk7XG5cbiAgICAtLWNhcmQtZmc6IHJnYig3MCwgNzAsIDcwKTtcbiAgICAtLWNhcmQtYmc6IHJnYigyNTAsIDI1MCwgMjUwKTtcblxuICAgICZbZGF0YS10aGVtZT1cImRhcmtcIl0ge1xuICAgICAgICAtLWJvZHktZmc6IHJnYigyNTIsIDI1MiwgMjUyKTtcbiAgICAgICAgLS1ib2R5LWJnOiByZ2IoNDUsIDQ1LCA0NSk7XG5cbiAgICAgICAgLS1oZWFkZXItZmc6IHJnYigyNDUsIDI0NSwgMjQ1KTtcbiAgICAgICAgLS1oZWFkZXItYmc6IHJnYig1MCwgNTAsIDUwKTtcblxuICAgICAgICAtLWNhcmQtZmc6IHJnYigyNTAsIDI1MCwgMjUwKTtcbiAgICAgICAgLS1jYXJkLWJnOiByZ2IoNzAsIDcwLCA3MCk7XG4gICAgfVxuXG59XG4iLCJAaW1wb3J0IHVybChcImh0dHBzOi8vZm9udHMuZ29vZ2xlYXBpcy5jb20vY3NzP2ZhbWlseT1MZXhlbmQrRGVjYXxOdW5pdG8rU2FucyZkaXNwbGF5PXN3YXBcIik7XG5odG1sIHtcbiAgLS1ib2R5LWZnOiByZ2IoNDUsIDQ1LCA0NSk7XG4gIC0tYm9keS1iZzogcmdiKDI1MiwgMjUyLCAyNTIpO1xuICAtLWhlYWRlci1mZzogcmdiKDQ1LCA0NSwgNDUpO1xuICAtLWhlYWRlci1iZzogcmdiKDI0NSwgMjQ1LCAyNDUpO1xuICAtLWNhcmQtZmc6IHJnYig3MCwgNzAsIDcwKTtcbiAgLS1jYXJkLWJnOiByZ2IoMjUwLCAyNTAsIDI1MCk7XG59XG5odG1sW2RhdGEtdGhlbWU9ZGFya10ge1xuICAtLWJvZHktZmc6IHJnYigyNTIsIDI1MiwgMjUyKTtcbiAgLS1ib2R5LWJnOiByZ2IoNDUsIDQ1LCA0NSk7XG4gIC0taGVhZGVyLWZnOiByZ2IoMjQ1LCAyNDUsIDI0NSk7XG4gIC0taGVhZGVyLWJnOiByZ2IoNTAsIDUwLCA1MCk7XG4gIC0tY2FyZC1mZzogcmdiKDI1MCwgMjUwLCAyNTApO1xuICAtLWNhcmQtYmc6IHJnYig3MCwgNzAsIDcwKTtcbn1cblxuI25hdmJhci1saXN0IHtcbiAgZGlzcGxheTogZmxleDtcbiAgZmxleC1kaXJlY3Rpb246IHJvdztcbiAganVzdGlmeS1jb250ZW50OiBjZW50ZXI7XG4gIGFsaWduLWl0ZW1zOiBjZW50ZXI7XG4gIHBhZGRpbmc6IDJlbSAzZW07XG4gIGNvbG9yOiB2YXIoLS1oZWFkZXItZmcpO1xuICBiYWNrZ3JvdW5kLWNvbG9yOiB2YXIoLS1oZWFkZXItYmcpO1xuICAtd2Via2l0LWJveC1zaGFkb3c6IDAgMCA2cHggMXB4IHJnYmEoMCwgMCwgMCwgMC4yKTtcbiAgLW1vei1ib3gtc2hhZG93OiAwIDAgNnB4IDFweCByZ2JhKDAsIDAsIDAsIDAuMik7XG4gIGJveC1zaGFkb3c6IDAgMCA2cHggMXB4IHJnYmEoMCwgMCwgMCwgMC4yKTtcbn1cbiNuYXZiYXItbGlzdCAubmF2YmFyLWxpbmsge1xuICBwYWRkaW5nLWxlZnQ6IDIuNzVlbTtcbiAgZm9udC1zaXplOiAxLjI1ZW07XG59XG4jbmF2YmFyLWxpc3QgLm5hdmJhci1saW5rI2hvbWUge1xuICBtYXJnaW4tcmlnaHQ6IGF1dG87XG4gIHBhZGRpbmctcmlnaHQ6IDE1cHg7XG4gIHBhZGRpbmctbGVmdDogMDtcbn1cbiNuYXZiYXItbGlzdCAubmF2YmFyLWxpbms6bm90KCNob21lKSBhIHtcbiAgY29sb3I6IHZhcigtLWhlYWRlci1mZyk7XG4gIHBhZGRpbmctYm90dG9tOiAwLjM1ZW07XG4gIGJvcmRlci1ib3R0b206IDFweCBzb2xpZCB2YXIoLS1oZWFkZXItZmcpO1xufSIsIkBpbXBvcnQgXCIuLi8uLi8uLi9zdHlsZXMvZm9udHNcIjtcbkBpbXBvcnQgXCIuLi8uLi8uLi9zdHlsZXMvY29sb3Vyc1wiO1xuXG4jbmF2YmFyLWxpc3Qge1xuXG4gICAgZGlzcGxheTogICAgICAgICAgIGZsZXg7XG4gICAgZmxleC1kaXJlY3Rpb246ICAgICByb3c7XG4gICAganVzdGlmeS1jb250ZW50OiBjZW50ZXI7XG4gICAgYWxpZ24taXRlbXM6ICAgICBjZW50ZXI7XG5cbiAgICBwYWRkaW5nOiAyZW0gM2VtO1xuXG4gICAgY29sb3I6IHZhcigtLWhlYWRlci1mZyk7XG4gICAgYmFja2dyb3VuZC1jb2xvcjogdmFyKC0taGVhZGVyLWJnKTtcblxuICAgICRib3gtc2hhZG93OiAwIDAgNnB4IDFweCByZ2JhKDAsIDAsIDAsIDAuMjApO1xuICAgIC13ZWJraXQtYm94LXNoYWRvdzogJGJveC1zaGFkb3c7XG4gICAgICAgLW1vei1ib3gtc2hhZG93OiAkYm94LXNoYWRvdztcbiAgICAgICAgICAgIGJveC1zaGFkb3c6ICRib3gtc2hhZG93O1xuXG4gICAgLm5hdmJhci1saW5rIHtcblxuICAgICAgICBwYWRkaW5nLWxlZnQ6IDIuNzVlbTtcblxuICAgICAgICBmb250LXNpemU6IDEuMjVlbTtcblxuICAgICAgICAmI2hvbWUge1xuICAgICAgICAgICAgbWFyZ2luLXJpZ2h0OiBhdXRvO1xuICAgICAgICAgICAgcGFkZGluZy1yaWdodDogMTVweDtcbiAgICAgICAgICAgIHBhZGRpbmctbGVmdDogMDtcbiAgICAgICAgfVxuXG4gICAgICAgICY6bm90KCNob21lKSBhIHtcbiAgICAgICAgICAgIGNvbG9yOiB2YXIoLS1oZWFkZXItZmcpO1xuXG4gICAgICAgICAgICBwYWRkaW5nLWJvdHRvbTogLjM1ZW07XG4gICAgICAgICAgICBib3JkZXItYm90dG9tOiAxcHggc29saWQgdmFyKC0taGVhZGVyLWZnKTs7XG4gICAgICAgIH1cblxuICAgIH1cblxufVxuIl19 */"

/***/ }),

/***/ "./src/app/components/navbar/navbar.component.ts":
/*!*******************************************************!*\
  !*** ./src/app/components/navbar/navbar.component.ts ***!
  \*******************************************************/
/*! exports provided: NavbarComponent */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "NavbarComponent", function() { return NavbarComponent; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "./node_modules/tslib/tslib.es6.js");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm5/core.js");
/* harmony import */ var _services_auth_auth_service__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ../../services/auth/auth.service */ "./src/app/services/auth/auth.service.ts");
/* harmony import */ var _angular_router__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! @angular/router */ "./node_modules/@angular/router/fesm5/router.js");




var NavbarComponent = /** @class */ (function () {
    function NavbarComponent(authService, router) {
        this.authService = authService;
        this.router = router;
    }
    NavbarComponent.prototype.ngOnInit = function () {
        console.log(this.authService.userToken);
    };
    NavbarComponent.prototype.navigateToProfile = function () {
        return tslib__WEBPACK_IMPORTED_MODULE_0__["__awaiter"](this, void 0, void 0, function () {
            var url, _a;
            return tslib__WEBPACK_IMPORTED_MODULE_0__["__generator"](this, function (_b) {
                switch (_b.label) {
                    case 0:
                        _a = "/profile/";
                        return [4 /*yield*/, this.authService.getUserUUID(this.authService.userToken)];
                    case 1:
                        url = _a + (_b.sent());
                        return [4 /*yield*/, this.router.navigateByUrl(url)];
                    case 2:
                        _b.sent();
                        return [2 /*return*/];
                }
            });
        });
    };
    NavbarComponent.ctorParameters = function () { return [
        { type: _services_auth_auth_service__WEBPACK_IMPORTED_MODULE_2__["AuthService"] },
        { type: _angular_router__WEBPACK_IMPORTED_MODULE_3__["Router"] }
    ]; };
    NavbarComponent = tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
        Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Component"])({
            selector: 'app-navbar',
            template: __webpack_require__(/*! raw-loader!./navbar.component.html */ "./node_modules/raw-loader/index.js!./src/app/components/navbar/navbar.component.html"),
            styles: [__webpack_require__(/*! ./navbar.component.scss */ "./src/app/components/navbar/navbar.component.scss")]
        })
    ], NavbarComponent);
    return NavbarComponent;
}());



/***/ }),

/***/ "./src/app/components/newarticle/new-article.component.scss":
/*!******************************************************************!*\
  !*** ./src/app/components/newarticle/new-article.component.scss ***!
  \******************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = "\n/*# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IiIsImZpbGUiOiJzcmMvYXBwL2NvbXBvbmVudHMvbmV3YXJ0aWNsZS9uZXctYXJ0aWNsZS5jb21wb25lbnQuc2NzcyJ9 */"

/***/ }),

/***/ "./src/app/components/newarticle/new-article.component.ts":
/*!****************************************************************!*\
  !*** ./src/app/components/newarticle/new-article.component.ts ***!
  \****************************************************************/
/*! exports provided: NewArticleComponent */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "NewArticleComponent", function() { return NewArticleComponent; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "./node_modules/tslib/tslib.es6.js");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm5/core.js");
/* harmony import */ var _models_Article__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ../../models/Article */ "./src/app/models/Article.ts");
/* harmony import */ var _services_article_article_service__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ../../services/article/article.service */ "./src/app/services/article/article.service.ts");
/* harmony import */ var _services_auth_auth_service__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! ../../services/auth/auth.service */ "./src/app/services/auth/auth.service.ts");





var NewArticleComponent = /** @class */ (function () {
    function NewArticleComponent(articleService, authService) {
        this.articleService = articleService;
        this.authService = authService;
        this.user = {
            username: 'lucy',
            uuid: '5fb72569-2086-46b8-b8a9-828fe5ce1bb6'
        };
        this.article = {
            uuid: '651fc79a-70cf-47ec-b85d-bac83df4cd15' /*'9c22b1ea-983c-48db-abd3-bd9c70a9816e'*/,
            title: '',
            categories: [],
            content: new _models_Article__WEBPACK_IMPORTED_MODULE_2__["Content"]('', ''),
            createdBy: { username: 'un', uuid: 'aa6e4b49-29c5-4028-a99e-96d5f93ef8ff' },
            createdAt: Date.now(),
        };
    }
    NewArticleComponent.prototype.createNewArticle = function () {
        var token = this.authService.userToken;
        console.log(token);
        console.log(this.article);
        var obs = this.articleService.createNewArticle(this.article, token);
        obs.then(function (it) { return console.log(it); });
    };
    NewArticleComponent.prototype.ngOnInit = function () {
        //console.log(await this.authService.currentUserToken)
    };
    NewArticleComponent.ctorParameters = function () { return [
        { type: _services_article_article_service__WEBPACK_IMPORTED_MODULE_3__["ArticleService"] },
        { type: _services_auth_auth_service__WEBPACK_IMPORTED_MODULE_4__["AuthService"] }
    ]; };
    NewArticleComponent = tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
        Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Component"])({
            selector: 'app-new-article',
            template: __webpack_require__(/*! raw-loader!./new-article.component.html */ "./node_modules/raw-loader/index.js!./src/app/components/newarticle/new-article.component.html"),
            styles: [__webpack_require__(/*! ./new-article.component.scss */ "./src/app/components/newarticle/new-article.component.scss")]
        })
    ], NewArticleComponent);
    return NewArticleComponent;
}());



/***/ }),

/***/ "./src/app/components/profile/profile.component.scss":
/*!***********************************************************!*\
  !*** ./src/app/components/profile/profile.component.scss ***!
  \***********************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = "\n/*# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IiIsImZpbGUiOiJzcmMvYXBwL2NvbXBvbmVudHMvcHJvZmlsZS9wcm9maWxlLmNvbXBvbmVudC5zY3NzIn0= */"

/***/ }),

/***/ "./src/app/components/profile/profile.component.ts":
/*!*********************************************************!*\
  !*** ./src/app/components/profile/profile.component.ts ***!
  \*********************************************************/
/*! exports provided: ProfileComponent */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "ProfileComponent", function() { return ProfileComponent; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "./node_modules/tslib/tslib.es6.js");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm5/core.js");
/* harmony import */ var _angular_router__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @angular/router */ "./node_modules/@angular/router/fesm5/router.js");
/* harmony import */ var src_app_services_auth_auth_service__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! src/app/services/auth/auth.service */ "./src/app/services/auth/auth.service.ts");




var ProfileComponent = /** @class */ (function () {
    function ProfileComponent(activatedRoute, authService) {
        this.activatedRoute = activatedRoute;
        this.authService = authService;
    }
    ProfileComponent.prototype.ngOnInit = function () {
        var _this = this;
        this.routeMapSubscription = this.activatedRoute.paramMap.subscribe(function (map) { return tslib__WEBPACK_IMPORTED_MODULE_0__["__awaiter"](_this, void 0, void 0, function () {
            var userUUID, _a;
            return tslib__WEBPACK_IMPORTED_MODULE_0__["__generator"](this, function (_b) {
                switch (_b.label) {
                    case 0:
                        userUUID = map.get('uuid');
                        _a = this;
                        return [4 /*yield*/, this.authService.getUser(userUUID)];
                    case 1:
                        _a.user = _b.sent();
                        console.log(userUUID);
                        console.log(this.user);
                        return [2 /*return*/];
                }
            });
        }); });
    };
    ProfileComponent.prototype.ngOnDestroy = function () {
        this.routeMapSubscription.unsubscribe();
    };
    ProfileComponent.ctorParameters = function () { return [
        { type: _angular_router__WEBPACK_IMPORTED_MODULE_2__["ActivatedRoute"] },
        { type: src_app_services_auth_auth_service__WEBPACK_IMPORTED_MODULE_3__["AuthService"] }
    ]; };
    ProfileComponent = tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
        Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Component"])({
            selector: 'app-profile',
            template: __webpack_require__(/*! raw-loader!./profile.component.html */ "./node_modules/raw-loader/index.js!./src/app/components/profile/profile.component.html"),
            styles: [__webpack_require__(/*! ./profile.component.scss */ "./src/app/components/profile/profile.component.scss")]
        })
    ], ProfileComponent);
    return ProfileComponent;
}());



/***/ }),

/***/ "./src/app/components/register/register.component.scss":
/*!*************************************************************!*\
  !*** ./src/app/components/register/register.component.scss ***!
  \*************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = "\n/*# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IiIsImZpbGUiOiJzcmMvYXBwL2NvbXBvbmVudHMvcmVnaXN0ZXIvcmVnaXN0ZXIuY29tcG9uZW50LnNjc3MifQ== */"

/***/ }),

/***/ "./src/app/components/register/register.component.ts":
/*!***********************************************************!*\
  !*** ./src/app/components/register/register.component.ts ***!
  \***********************************************************/
/*! exports provided: RegisterComponent */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "RegisterComponent", function() { return RegisterComponent; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "./node_modules/tslib/tslib.es6.js");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm5/core.js");
/* harmony import */ var _services_auth_auth_service__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ../../services/auth/auth.service */ "./src/app/services/auth/auth.service.ts");



var RegisterComponent = /** @class */ (function () {
    function RegisterComponent(authService) {
        this.authService = authService;
        this.user = { name: '', username: '', password: '', email: '' };
        this.ngOnInit();
    }
    RegisterComponent.prototype.ngOnInit = function () {
    };
    //TODO: add async methods for client display
    RegisterComponent.prototype.register = function () {
        var _this = this;
        console.log(this.user);
        this.authService.register(this.user).subscribe(function (it) {
            _this.user = it;
            console.log(_this.user);
            return _this.user;
        });
    };
    RegisterComponent.ctorParameters = function () { return [
        { type: _services_auth_auth_service__WEBPACK_IMPORTED_MODULE_2__["AuthService"] }
    ]; };
    RegisterComponent = tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
        Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Component"])({
            selector: 'app-register',
            template: __webpack_require__(/*! raw-loader!./register.component.html */ "./node_modules/raw-loader/index.js!./src/app/components/register/register.component.html"),
            styles: [__webpack_require__(/*! ./register.component.scss */ "./src/app/components/register/register.component.scss")]
        })
    ], RegisterComponent);
    return RegisterComponent;
}());



/***/ }),

/***/ "./src/app/components/search/search.component.scss":
/*!*********************************************************!*\
  !*** ./src/app/components/search/search.component.scss ***!
  \*********************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = "\n/*# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IiIsImZpbGUiOiJzcmMvYXBwL2NvbXBvbmVudHMvc2VhcmNoL3NlYXJjaC5jb21wb25lbnQuc2NzcyJ9 */"

/***/ }),

/***/ "./src/app/components/search/search.component.ts":
/*!*******************************************************!*\
  !*** ./src/app/components/search/search.component.ts ***!
  \*******************************************************/
/*! exports provided: SearchComponent */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "SearchComponent", function() { return SearchComponent; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "./node_modules/tslib/tslib.es6.js");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm5/core.js");


var SearchComponent = /** @class */ (function () {
    function SearchComponent() {
    }
    SearchComponent.prototype.ngOnInit = function () {
    };
    SearchComponent = tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
        Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Component"])({
            selector: 'app-search',
            template: __webpack_require__(/*! raw-loader!./search.component.html */ "./node_modules/raw-loader/index.js!./src/app/components/search/search.component.html"),
            styles: [__webpack_require__(/*! ./search.component.scss */ "./src/app/components/search/search.component.scss")]
        })
    ], SearchComponent);
    return SearchComponent;
}());



/***/ }),

/***/ "./src/app/components/show-all-articles/show-all-articles.component.scss":
/*!*******************************************************************************!*\
  !*** ./src/app/components/show-all-articles/show-all-articles.component.scss ***!
  \*******************************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = "@import url(\"https://fonts.googleapis.com/css?family=Lexend+Deca|Nunito+Sans&display=swap\");\nhtml {\n  --body-fg: rgb(45, 45, 45);\n  --body-bg: rgb(252, 252, 252);\n  --header-fg: rgb(45, 45, 45);\n  --header-bg: rgb(245, 245, 245);\n  --card-fg: rgb(70, 70, 70);\n  --card-bg: rgb(250, 250, 250);\n}\nhtml[data-theme=dark] {\n  --body-fg: rgb(252, 252, 252);\n  --body-bg: rgb(45, 45, 45);\n  --header-fg: rgb(245, 245, 245);\n  --header-bg: rgb(50, 50, 50);\n  --card-fg: rgb(250, 250, 250);\n  --card-bg: rgb(70, 70, 70);\n}\n.main {\n  background-color: var(--body-bg);\n  padding: 3em;\n}\n.main .articles {\n  width: calc((100% / 3) * 2);\n}\n.main .articles #articles-header {\n  display: flex;\n  flex-direction: row;\n  justify-content: space-between;\n  align-items: center;\n}\n.main .articles #articles-header #header-create-btn {\n  font-size: 2em;\n  font-weight: bold;\n  cursor: pointer;\n}\n.main .articles .article {\n  display: flex;\n  flex-direction: column;\n  justify-content: center;\n  align-items: flex-start;\n  color: var(--card-fg);\n  background: var(--card-bg);\n  border-radius: 5px;\n  border: none;\n  box-shadow: 0 0 6px 1px rgba(0, 0, 0, 0.2);\n  padding: 1.5em;\n  margin-top: 2%;\n}\n.main .articles .article .article-first-line {\n  width: 100%;\n  text-align: center;\n  display: flex;\n  flex-direction: row;\n  justify-content: space-between;\n  align-items: center;\n}\n.main .articles .article .article-first-line .article-title {\n  font-size: 30px;\n}\n.main .articles .article .article-summary {\n  font-size: 1.25em;\n}\n.main .articles .article .read-more {\n  color: var(--card-fg);\n}\n.main .articles .article .article-comments-count {\n  align-self: flex-end;\n}\n.main .articles .article .article-no-tags,\n.main .articles .article .article-tags {\n  display: flex;\n  align-self: flex-end;\n}\n.main .articles .article .tag-list {\n  background: white;\n  box-shadow: 0 0 5px 1px rgba(0, 0, 0, 0.26);\n  display: flex;\n  padding: 1%;\n  margin-left: 1%;\n  border-radius: 5px;\n}\n@media screen and (max-width: 400px) {\n  .sidebar a {\n    text-align: center;\n    float: none;\n  }\n}\n/*# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi9ob21lL2x1Y3lhZ2FtYWl0ZS9ibG9naWZ5L3NyYy9ibG9naWZ5L2Zyb250ZW5kL3NyYy9hcHAvY29tcG9uZW50cy9zaG93LWFsbC1hcnRpY2xlcy9zaG93LWFsbC1hcnRpY2xlcy5jb21wb25lbnQuc2NzcyIsIi9ob21lL2x1Y3lhZ2FtYWl0ZS9ibG9naWZ5L3NyYy9ibG9naWZ5L2Zyb250ZW5kL3NyYy9zdHlsZXMvY29sb3Vycy5zY3NzIiwic3JjL2FwcC9jb21wb25lbnRzL3Nob3ctYWxsLWFydGljbGVzL3Nob3ctYWxsLWFydGljbGVzLmNvbXBvbmVudC5zY3NzIiwiL2hvbWUvbHVjeWFnYW1haXRlL2Jsb2dpZnkvc3JjL2Jsb2dpZnkvZnJvbnRlbmQvc3RkaW4iLCIvaG9tZS9sdWN5YWdhbWFpdGUvYmxvZ2lmeS9zcmMvYmxvZ2lmeS9mcm9udGVuZC9zcmMvc3R5bGVzL2xheW91dHMuc2NzcyJdLCJuYW1lcyI6W10sIm1hcHBpbmdzIjoiQUFBUSwyRkFBQTtBQ0FSO0VBRUksMEJBQUE7RUFDQSw2QkFBQTtFQUVBLDRCQUFBO0VBQ0EsK0JBQUE7RUFFQSwwQkFBQTtFQUNBLDZCQUFBO0FDREo7QURHSTtFQUNJLDZCQUFBO0VBQ0EsMEJBQUE7RUFFQSwrQkFBQTtFQUNBLDRCQUFBO0VBRUEsNkJBQUE7RUFDQSwwQkFBQTtBQ0hSO0FDWkE7RUFFSSxnQ0FBQTtFQUVBLFlBQUE7QURhSjtBQ1hJO0VBRUksMkJBQUE7QURZUjtBQ1ZRO0VBQ0ksYUFBQTtFQUNBLG1CQUFBO0VBQ0EsOEJBQUE7RUFDQSxtQkFBQTtBRFlaO0FDVlk7RUFDSSxjQUFBO0VBQ0EsaUJBQUE7RUFFQSxlQUFBO0FEV2hCO0FDUFE7RUFFSSxhQUFBO0VBQ0Esc0JBQUE7RUFDQSx1QkFBQTtFQUNBLHVCQUFBO0VBRUEscUJBQUE7RUFDQSwwQkFBQTtFQUVBLGtCQ3RDUTtFRHVDUixZQUFBO0VBS1EsMENBSEs7RUFLYixjQUFBO0VBRUEsY0FBQTtBREVaO0FDQVk7RUFDSSxXQUFBO0VBRUEsa0JBQUE7RUFFQSxhQUFBO0VBQ0EsbUJBQUE7RUFDQSw4QkFBQTtFQUNBLG1CQUFBO0FEQWhCO0FDRWdCO0VBQWtCLGVBQUE7QURDbEM7QUNFWTtFQUFtQixpQkFBQTtBREMvQjtBQ0NZO0VBQWEscUJBQUE7QURFekI7QUNBWTtFQUEwQixvQkFBQTtBREd0QztBQ0RZOztFQUNnQixhQUFBO0VBQWUsb0JBQUE7QURLM0M7QUNIWTtFQUNJLGlCQUFBO0VBR0EsMkNBQUE7RUFDQSxhQUFBO0VBQ0EsV0FBQTtFQUNBLGVBQUE7RUFDQSxrQkFBQTtBREtoQjtBQ0lBO0VBQ0k7SUFDSSxrQkFBQTtJQUNBLFdBQUE7RURETjtBQUNGIiwiZmlsZSI6InNyYy9hcHAvY29tcG9uZW50cy9zaG93LWFsbC1hcnRpY2xlcy9zaG93LWFsbC1hcnRpY2xlcy5jb21wb25lbnQuc2NzcyIsInNvdXJjZXNDb250ZW50IjpbIkBpbXBvcnQgdXJsKCdodHRwczovL2ZvbnRzLmdvb2dsZWFwaXMuY29tL2Nzcz9mYW1pbHk9TGV4ZW5kK0RlY2F8TnVuaXRvK1NhbnMmZGlzcGxheT1zd2FwJyk7XG5cbiRsZXhlbmRhOiAnTGV4ZW5kIERlY2EnLCBzYW5zLXNlcmlmO1xuJG51bml0bzogJ051bml0byBTYW5zJywgc2Fucy1zZXJpZjtcbiIsImh0bWwge1xuXG4gICAgLS1ib2R5LWZnOiByZ2IoNDUsIDQ1LCA0NSk7XG4gICAgLS1ib2R5LWJnOiByZ2IoMjUyLCAyNTIsIDI1Mik7XG5cbiAgICAtLWhlYWRlci1mZzogcmdiKDQ1LCA0NSwgNDUpO1xuICAgIC0taGVhZGVyLWJnOiByZ2IoMjQ1LCAyNDUsIDI0NSk7XG5cbiAgICAtLWNhcmQtZmc6IHJnYig3MCwgNzAsIDcwKTtcbiAgICAtLWNhcmQtYmc6IHJnYigyNTAsIDI1MCwgMjUwKTtcblxuICAgICZbZGF0YS10aGVtZT1cImRhcmtcIl0ge1xuICAgICAgICAtLWJvZHktZmc6IHJnYigyNTIsIDI1MiwgMjUyKTtcbiAgICAgICAgLS1ib2R5LWJnOiByZ2IoNDUsIDQ1LCA0NSk7XG5cbiAgICAgICAgLS1oZWFkZXItZmc6IHJnYigyNDUsIDI0NSwgMjQ1KTtcbiAgICAgICAgLS1oZWFkZXItYmc6IHJnYig1MCwgNTAsIDUwKTtcblxuICAgICAgICAtLWNhcmQtZmc6IHJnYigyNTAsIDI1MCwgMjUwKTtcbiAgICAgICAgLS1jYXJkLWJnOiByZ2IoNzAsIDcwLCA3MCk7XG4gICAgfVxuXG59XG4iLCJAaW1wb3J0IHVybChcImh0dHBzOi8vZm9udHMuZ29vZ2xlYXBpcy5jb20vY3NzP2ZhbWlseT1MZXhlbmQrRGVjYXxOdW5pdG8rU2FucyZkaXNwbGF5PXN3YXBcIik7XG5odG1sIHtcbiAgLS1ib2R5LWZnOiByZ2IoNDUsIDQ1LCA0NSk7XG4gIC0tYm9keS1iZzogcmdiKDI1MiwgMjUyLCAyNTIpO1xuICAtLWhlYWRlci1mZzogcmdiKDQ1LCA0NSwgNDUpO1xuICAtLWhlYWRlci1iZzogcmdiKDI0NSwgMjQ1LCAyNDUpO1xuICAtLWNhcmQtZmc6IHJnYig3MCwgNzAsIDcwKTtcbiAgLS1jYXJkLWJnOiByZ2IoMjUwLCAyNTAsIDI1MCk7XG59XG5odG1sW2RhdGEtdGhlbWU9ZGFya10ge1xuICAtLWJvZHktZmc6IHJnYigyNTIsIDI1MiwgMjUyKTtcbiAgLS1ib2R5LWJnOiByZ2IoNDUsIDQ1LCA0NSk7XG4gIC0taGVhZGVyLWZnOiByZ2IoMjQ1LCAyNDUsIDI0NSk7XG4gIC0taGVhZGVyLWJnOiByZ2IoNTAsIDUwLCA1MCk7XG4gIC0tY2FyZC1mZzogcmdiKDI1MCwgMjUwLCAyNTApO1xuICAtLWNhcmQtYmc6IHJnYig3MCwgNzAsIDcwKTtcbn1cblxuLm1haW4ge1xuICBiYWNrZ3JvdW5kLWNvbG9yOiB2YXIoLS1ib2R5LWJnKTtcbiAgcGFkZGluZzogM2VtO1xufVxuLm1haW4gLmFydGljbGVzIHtcbiAgd2lkdGg6IGNhbGMoKDEwMCUgLyAzKSAqIDIpO1xufVxuLm1haW4gLmFydGljbGVzICNhcnRpY2xlcy1oZWFkZXIge1xuICBkaXNwbGF5OiBmbGV4O1xuICBmbGV4LWRpcmVjdGlvbjogcm93O1xuICBqdXN0aWZ5LWNvbnRlbnQ6IHNwYWNlLWJldHdlZW47XG4gIGFsaWduLWl0ZW1zOiBjZW50ZXI7XG59XG4ubWFpbiAuYXJ0aWNsZXMgI2FydGljbGVzLWhlYWRlciAjaGVhZGVyLWNyZWF0ZS1idG4ge1xuICBmb250LXNpemU6IDJlbTtcbiAgZm9udC13ZWlnaHQ6IGJvbGQ7XG4gIGN1cnNvcjogcG9pbnRlcjtcbn1cbi5tYWluIC5hcnRpY2xlcyAuYXJ0aWNsZSB7XG4gIGRpc3BsYXk6IGZsZXg7XG4gIGZsZXgtZGlyZWN0aW9uOiBjb2x1bW47XG4gIGp1c3RpZnktY29udGVudDogY2VudGVyO1xuICBhbGlnbi1pdGVtczogZmxleC1zdGFydDtcbiAgY29sb3I6IHZhcigtLWNhcmQtZmcpO1xuICBiYWNrZ3JvdW5kOiB2YXIoLS1jYXJkLWJnKTtcbiAgYm9yZGVyLXJhZGl1czogNXB4O1xuICBib3JkZXI6IG5vbmU7XG4gIC13ZWJraXQtYm94LXNoYWRvdzogMCAwIDZweCAxcHggcmdiYSgwLCAwLCAwLCAwLjIpO1xuICAtbW96LWJveC1zaGFkb3c6IDAgMCA2cHggMXB4IHJnYmEoMCwgMCwgMCwgMC4yKTtcbiAgYm94LXNoYWRvdzogMCAwIDZweCAxcHggcmdiYSgwLCAwLCAwLCAwLjIpO1xuICBwYWRkaW5nOiAxLjVlbTtcbiAgbWFyZ2luLXRvcDogMiU7XG59XG4ubWFpbiAuYXJ0aWNsZXMgLmFydGljbGUgLmFydGljbGUtZmlyc3QtbGluZSB7XG4gIHdpZHRoOiAxMDAlO1xuICB0ZXh0LWFsaWduOiBjZW50ZXI7XG4gIGRpc3BsYXk6IGZsZXg7XG4gIGZsZXgtZGlyZWN0aW9uOiByb3c7XG4gIGp1c3RpZnktY29udGVudDogc3BhY2UtYmV0d2VlbjtcbiAgYWxpZ24taXRlbXM6IGNlbnRlcjtcbn1cbi5tYWluIC5hcnRpY2xlcyAuYXJ0aWNsZSAuYXJ0aWNsZS1maXJzdC1saW5lIC5hcnRpY2xlLXRpdGxlIHtcbiAgZm9udC1zaXplOiAzMHB4O1xufVxuLm1haW4gLmFydGljbGVzIC5hcnRpY2xlIC5hcnRpY2xlLXN1bW1hcnkge1xuICBmb250LXNpemU6IDEuMjVlbTtcbn1cbi5tYWluIC5hcnRpY2xlcyAuYXJ0aWNsZSAucmVhZC1tb3JlIHtcbiAgY29sb3I6IHZhcigtLWNhcmQtZmcpO1xufVxuLm1haW4gLmFydGljbGVzIC5hcnRpY2xlIC5hcnRpY2xlLWNvbW1lbnRzLWNvdW50IHtcbiAgYWxpZ24tc2VsZjogZmxleC1lbmQ7XG59XG4ubWFpbiAuYXJ0aWNsZXMgLmFydGljbGUgLmFydGljbGUtbm8tdGFncyxcbi5tYWluIC5hcnRpY2xlcyAuYXJ0aWNsZSAuYXJ0aWNsZS10YWdzIHtcbiAgZGlzcGxheTogZmxleDtcbiAgYWxpZ24tc2VsZjogZmxleC1lbmQ7XG59XG4ubWFpbiAuYXJ0aWNsZXMgLmFydGljbGUgLnRhZy1saXN0IHtcbiAgYmFja2dyb3VuZDogd2hpdGU7XG4gIC13ZWJraXQtYm94LXNoYWRvdzogMCAwIDVweCAxcHggcmdiYSgwLCAwLCAwLCAwLjI2KTtcbiAgLW1vei1ib3gtc2hhZG93OiAwIDAgNXB4IDFweCByZ2JhKDAsIDAsIDAsIDAuMjYpO1xuICBib3gtc2hhZG93OiAwIDAgNXB4IDFweCByZ2JhKDAsIDAsIDAsIDAuMjYpO1xuICBkaXNwbGF5OiBmbGV4O1xuICBwYWRkaW5nOiAxJTtcbiAgbWFyZ2luLWxlZnQ6IDElO1xuICBib3JkZXItcmFkaXVzOiA1cHg7XG59XG5cbkBtZWRpYSBzY3JlZW4gYW5kIChtYXgtd2lkdGg6IDQwMHB4KSB7XG4gIC5zaWRlYmFyIGEge1xuICAgIHRleHQtYWxpZ246IGNlbnRlcjtcbiAgICBmbG9hdDogbm9uZTtcbiAgfVxufSIsIkBpbXBvcnQgXCIuLi8uLi8uLi9zdHlsZXMvZm9udHNcIjtcbkBpbXBvcnQgXCIuLi8uLi8uLi9zdHlsZXMvY29sb3Vyc1wiO1xuQGltcG9ydCBcIi4uLy4uLy4uL3N0eWxlcy9sYXlvdXRzXCI7XG5cbi5tYWluIHtcblxuICAgIGJhY2tncm91bmQtY29sb3I6IHZhcigtLWJvZHktYmcpO1xuXG4gICAgcGFkZGluZzogM2VtO1xuXG4gICAgLmFydGljbGVzIHtcblxuICAgICAgICB3aWR0aDogY2FsYygoMTAwJSAvIDMpICogMik7XG5cbiAgICAgICAgI2FydGljbGVzLWhlYWRlciB7XG4gICAgICAgICAgICBkaXNwbGF5OiAgICAgICAgICAgICAgICAgIGZsZXg7XG4gICAgICAgICAgICBmbGV4LWRpcmVjdGlvbjogICAgICAgICAgICByb3c7XG4gICAgICAgICAgICBqdXN0aWZ5LWNvbnRlbnQ6IHNwYWNlLWJldHdlZW47XG4gICAgICAgICAgICBhbGlnbi1pdGVtczogICAgICAgICAgICBjZW50ZXI7XG5cbiAgICAgICAgICAgICNoZWFkZXItY3JlYXRlLWJ0biB7XG4gICAgICAgICAgICAgICAgZm9udC1zaXplOiAyZW07XG4gICAgICAgICAgICAgICAgZm9udC13ZWlnaHQ6IGJvbGQ7XG5cbiAgICAgICAgICAgICAgICBjdXJzb3I6IHBvaW50ZXI7XG4gICAgICAgICAgICB9XG4gICAgICAgIH1cblxuICAgICAgICAuYXJ0aWNsZSB7XG5cbiAgICAgICAgICAgIGRpc3BsYXk6ICAgICAgICAgZmxleDtcbiAgICAgICAgICAgIGZsZXgtZGlyZWN0aW9uOiAgY29sdW1uO1xuICAgICAgICAgICAganVzdGlmeS1jb250ZW50OiBjZW50ZXI7XG4gICAgICAgICAgICBhbGlnbi1pdGVtczogICAgIGZsZXgtc3RhcnQ7XG5cbiAgICAgICAgICAgIGNvbG9yOiB2YXIoLS1jYXJkLWZnKTtcbiAgICAgICAgICAgIGJhY2tncm91bmQ6IHZhcigtLWNhcmQtYmcpO1xuXG4gICAgICAgICAgICBib3JkZXItcmFkaXVzOiAkc3RkLWJvcmRlci1yYWRpdXM7XG4gICAgICAgICAgICBib3JkZXI6IG5vbmU7XG5cbiAgICAgICAgICAgICRib3gtc2hhZG93OiAwIDAgNnB4IDFweCByZ2JhKDAsIDAsIDAsIDAuMjApO1xuICAgICAgICAgICAgLXdlYmtpdC1ib3gtc2hhZG93OiAkYm94LXNoYWRvdztcbiAgICAgICAgICAgICAgIC1tb3otYm94LXNoYWRvdzogJGJveC1zaGFkb3c7XG4gICAgICAgICAgICAgICAgICAgIGJveC1zaGFkb3c6ICRib3gtc2hhZG93OztcblxuICAgICAgICAgICAgcGFkZGluZzogMS41ZW07XG5cbiAgICAgICAgICAgIG1hcmdpbi10b3A6IDIlO1xuXG4gICAgICAgICAgICAuYXJ0aWNsZS1maXJzdC1saW5lIHtcbiAgICAgICAgICAgICAgICB3aWR0aDogMTAwJTtcblxuICAgICAgICAgICAgICAgIHRleHQtYWxpZ246IGNlbnRlcjtcblxuICAgICAgICAgICAgICAgIGRpc3BsYXk6ICAgICAgICAgICAgICAgICAgZmxleDtcbiAgICAgICAgICAgICAgICBmbGV4LWRpcmVjdGlvbjogICAgICAgICAgICByb3c7XG4gICAgICAgICAgICAgICAganVzdGlmeS1jb250ZW50OiBzcGFjZS1iZXR3ZWVuO1xuICAgICAgICAgICAgICAgIGFsaWduLWl0ZW1zOiAgICAgICAgICAgIGNlbnRlcjtcblxuICAgICAgICAgICAgICAgIC5hcnRpY2xlLXRpdGxlICB7IGZvbnQtc2l6ZTogMzBweDsgfVxuICAgICAgICAgICAgfVxuXG4gICAgICAgICAgICAuYXJ0aWNsZS1zdW1tYXJ5IHsgZm9udC1zaXplOiAxLjI1ZW07IH1cblxuICAgICAgICAgICAgLnJlYWQtbW9yZSB7IGNvbG9yOiB2YXIoLS1jYXJkLWZnKTsgfVxuXG4gICAgICAgICAgICAuYXJ0aWNsZS1jb21tZW50cy1jb3VudCB7IGFsaWduLXNlbGY6IGZsZXgtZW5kOyB9XG5cbiAgICAgICAgICAgIC5hcnRpY2xlLW5vLXRhZ3MsXG4gICAgICAgICAgICAuYXJ0aWNsZS10YWdzIHsgZGlzcGxheTogZmxleDsgYWxpZ24tc2VsZjogZmxleC1lbmQ7IH1cblxuICAgICAgICAgICAgLnRhZy1saXN0IHtcbiAgICAgICAgICAgICAgICBiYWNrZ3JvdW5kOiB3aGl0ZTtcbiAgICAgICAgICAgICAgICAtd2Via2l0LWJveC1zaGFkb3c6IDAgMCA1cHggMXB4IHJnYmEoMCwwLDAsMC4yNik7XG4gICAgICAgICAgICAgICAgLW1vei1ib3gtc2hhZG93OiAwIDAgNXB4IDFweCByZ2JhKDAsMCwwLDAuMjYpO1xuICAgICAgICAgICAgICAgIGJveC1zaGFkb3c6IDAgMCA1cHggMXB4IHJnYmEoMCwwLDAsMC4yNik7XG4gICAgICAgICAgICAgICAgZGlzcGxheTogZmxleDtcbiAgICAgICAgICAgICAgICBwYWRkaW5nOiAxJTtcbiAgICAgICAgICAgICAgICBtYXJnaW4tbGVmdDogMSU7XG4gICAgICAgICAgICAgICAgYm9yZGVyLXJhZGl1czogNXB4O1xuICAgICAgICAgICAgfVxuXG4gICAgICAgIH1cblxuICAgIH1cblxufVxuXG5AbWVkaWEgc2NyZWVuIGFuZCAobWF4LXdpZHRoOiA0MDBweCkge1xuICAgIC5zaWRlYmFyIGEge1xuICAgICAgICB0ZXh0LWFsaWduOiBjZW50ZXI7XG4gICAgICAgIGZsb2F0OiBub25lO1xuICAgIH1cbn1cbiIsIiRzdGQtYm9yZGVyLXJhZGl1czogNXB4O1xuIl19 */"

/***/ }),

/***/ "./src/app/components/show-all-articles/show-all-articles.component.ts":
/*!*****************************************************************************!*\
  !*** ./src/app/components/show-all-articles/show-all-articles.component.ts ***!
  \*****************************************************************************/
/*! exports provided: ShowAllArticlesComponent */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "ShowAllArticlesComponent", function() { return ShowAllArticlesComponent; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "./node_modules/tslib/tslib.es6.js");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm5/core.js");
/* harmony import */ var _services_article_article_service__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ../../services/article/article.service */ "./src/app/services/article/article.service.ts");
/* harmony import */ var _services_auth_auth_service__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ../../services/auth/auth.service */ "./src/app/services/auth/auth.service.ts");
/* harmony import */ var _angular_router__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! @angular/router */ "./node_modules/@angular/router/fesm5/router.js");





var ShowAllArticlesComponent = /** @class */ (function () {
    function ShowAllArticlesComponent(articleService, authService, router) {
        this.articleService = articleService;
        this.authService = authService;
        this.router = router;
    }
    ShowAllArticlesComponent.prototype.ngOnInit = function () {
        var _this = this;
        this.articleService.getAllArticles().then(function (it) {
            _this.articles = it;
        });
    };
    ShowAllArticlesComponent.prototype.navigateToNewArticle = function () {
        return tslib__WEBPACK_IMPORTED_MODULE_0__["__awaiter"](this, void 0, void 0, function () {
            return tslib__WEBPACK_IMPORTED_MODULE_0__["__generator"](this, function (_a) {
                switch (_a.label) {
                    case 0:
                        if (!(this.authService.userToken == '')) return [3 /*break*/, 2];
                        return [4 /*yield*/, this.router.navigateByUrl('/login')];
                    case 1:
                        _a.sent();
                        return [3 /*break*/, 4];
                    case 2: return [4 /*yield*/, this.router.navigateByUrl('/new-article')];
                    case 3:
                        _a.sent();
                        _a.label = 4;
                    case 4: return [2 /*return*/];
                }
            });
        });
    };
    ShowAllArticlesComponent.ctorParameters = function () { return [
        { type: _services_article_article_service__WEBPACK_IMPORTED_MODULE_2__["ArticleService"] },
        { type: _services_auth_auth_service__WEBPACK_IMPORTED_MODULE_3__["AuthService"] },
        { type: _angular_router__WEBPACK_IMPORTED_MODULE_4__["Router"] }
    ]; };
    ShowAllArticlesComponent = tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
        Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Component"])({
            selector: 'app-show-all-articles',
            template: __webpack_require__(/*! raw-loader!./show-all-articles.component.html */ "./node_modules/raw-loader/index.js!./src/app/components/show-all-articles/show-all-articles.component.html"),
            styles: [__webpack_require__(/*! ./show-all-articles.component.scss */ "./src/app/components/show-all-articles/show-all-articles.component.scss")]
        })
    ], ShowAllArticlesComponent);
    return ShowAllArticlesComponent;
}());



/***/ }),

/***/ "./src/app/components/show-article/show-article.component.scss":
/*!*********************************************************************!*\
  !*** ./src/app/components/show-article/show-article.component.scss ***!
  \*********************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = "\n/*# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IiIsImZpbGUiOiJzcmMvYXBwL2NvbXBvbmVudHMvc2hvdy1hcnRpY2xlL3Nob3ctYXJ0aWNsZS5jb21wb25lbnQuc2NzcyJ9 */"

/***/ }),

/***/ "./src/app/components/show-article/show-article.component.ts":
/*!*******************************************************************!*\
  !*** ./src/app/components/show-article/show-article.component.ts ***!
  \*******************************************************************/
/*! exports provided: ShowArticleComponent */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "ShowArticleComponent", function() { return ShowArticleComponent; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "./node_modules/tslib/tslib.es6.js");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm5/core.js");
/* harmony import */ var _angular_router__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @angular/router */ "./node_modules/@angular/router/fesm5/router.js");
/* harmony import */ var _services_article_article_service__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ../../services/article/article.service */ "./src/app/services/article/article.service.ts");
/* harmony import */ var _services_auth_auth_service__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! ../../services/auth/auth.service */ "./src/app/services/auth/auth.service.ts");





var ShowArticleComponent = /** @class */ (function () {
    function ShowArticleComponent(activatedRoute, articleService, authService) {
        this.activatedRoute = activatedRoute;
        this.articleService = articleService;
        this.authService = authService;
    }
    ShowArticleComponent.prototype.ngOnInit = function () {
        var _this = this;
        this.routeMapSubscription = this.activatedRoute.paramMap.subscribe(function (map) { return tslib__WEBPACK_IMPORTED_MODULE_0__["__awaiter"](_this, void 0, void 0, function () {
            var articleUUID, _a, _b, _c;
            return tslib__WEBPACK_IMPORTED_MODULE_0__["__generator"](this, function (_d) {
                switch (_d.label) {
                    case 0:
                        articleUUID = map.get('uuid');
                        console.log(articleUUID);
                        _a = this;
                        return [4 /*yield*/, this.articleService.getArticleByUUID(articleUUID).toPromise()];
                    case 1:
                        _a.article = _d.sent();
                        console.log(this.article);
                        _b = this;
                        return [4 /*yield*/, this.articleService.getArticleContent(articleUUID).toPromise()];
                    case 2:
                        _b.articleContent = _d.sent();
                        console.log(this.articleContent);
                        _c = this;
                        return [4 /*yield*/, this.authService.getUser(this.article.createdBy.toString())];
                    case 3:
                        _c.articleAuthor = _d.sent();
                        console.log(this.articleAuthor.username);
                        return [2 /*return*/];
                }
            });
        }); });
    };
    ShowArticleComponent.prototype.convertTimeStampToHumanDate = function (time) {
        return new Date(time).toDateString();
    };
    ShowArticleComponent.ctorParameters = function () { return [
        { type: _angular_router__WEBPACK_IMPORTED_MODULE_2__["ActivatedRoute"] },
        { type: _services_article_article_service__WEBPACK_IMPORTED_MODULE_3__["ArticleService"] },
        { type: _services_auth_auth_service__WEBPACK_IMPORTED_MODULE_4__["AuthService"] }
    ]; };
    ShowArticleComponent = tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
        Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Component"])({
            selector: 'app-show-article',
            template: __webpack_require__(/*! raw-loader!./show-article.component.html */ "./node_modules/raw-loader/index.js!./src/app/components/show-article/show-article.component.html"),
            styles: [__webpack_require__(/*! ./show-article.component.scss */ "./src/app/components/show-article/show-article.component.scss")]
        })
    ], ShowArticleComponent);
    return ShowArticleComponent;
}());



/***/ }),

/***/ "./src/app/models/Article.ts":
/*!***********************************!*\
  !*** ./src/app/models/Article.ts ***!
  \***********************************/
/*! exports provided: Article, Content */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "Article", function() { return Article; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "Content", function() { return Content; });
/* harmony import */ var _User__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./User */ "./src/app/models/User.ts");

var Article = /** @class */ (function () {
    function Article(uuid, title, content, createdBy, createdAt, categories) {
        this.uuid = uuid;
        this.title = title;
        this.content = content;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.categories = categories;
    }
    Article.ctorParameters = function () { return [
        { type: String },
        { type: String },
        { type: Content },
        { type: _User__WEBPACK_IMPORTED_MODULE_0__["User"] },
        { type: Number },
        { type: Array }
    ]; };
    return Article;
}());

var Content = /** @class */ (function () {
    function Content(text, summary) {
        this.text = text;
        this.summary = summary;
    }
    Content.ctorParameters = function () { return [
        { type: String },
        { type: String }
    ]; };
    return Content;
}());



/***/ }),

/***/ "./src/app/models/User.ts":
/*!********************************!*\
  !*** ./src/app/models/User.ts ***!
  \********************************/
/*! exports provided: User, LoginCredentials, RegisterCredentials */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "User", function() { return User; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "LoginCredentials", function() { return LoginCredentials; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "RegisterCredentials", function() { return RegisterCredentials; });
var User = /** @class */ (function () {
    function User(username, uuid) {
        this.username = username;
        this.uuid = uuid;
    }
    User.ctorParameters = function () { return [
        { type: String },
        { type: String }
    ]; };
    return User;
}());

var LoginCredentials = /** @class */ (function () {
    function LoginCredentials(username, password) {
        this.username = username;
        this.password = password;
    }
    LoginCredentials.ctorParameters = function () { return [
        { type: String },
        { type: String }
    ]; };
    return LoginCredentials;
}());

var RegisterCredentials = /** @class */ (function () {
    function RegisterCredentials(name, username, password, email) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.email = email;
    }
    RegisterCredentials.ctorParameters = function () { return [
        { type: String },
        { type: String },
        { type: String },
        { type: String }
    ]; };
    return RegisterCredentials;
}());



/***/ }),

/***/ "./src/app/services/article/article.service.ts":
/*!*****************************************************!*\
  !*** ./src/app/services/article/article.service.ts ***!
  \*****************************************************/
/*! exports provided: ArticleService */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "ArticleService", function() { return ArticleService; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "./node_modules/tslib/tslib.es6.js");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm5/core.js");
/* harmony import */ var _angular_common_http__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @angular/common/http */ "./node_modules/@angular/common/fesm5/http.js");
/* harmony import */ var _auth_auth_service__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ../auth/auth.service */ "./src/app/services/auth/auth.service.ts");




var ArticleService = /** @class */ (function () {
    function ArticleService(httpClient, authService) {
        this.httpClient = httpClient;
        this.authService = authService;
    }
    ArticleService.prototype.getAllArticles = function () {
        return tslib__WEBPACK_IMPORTED_MODULE_0__["__awaiter"](this, void 0, void 0, function () {
            var articlesObs, articles, out, articles_1, articles_1_1, it, copy, promises, e_1_1;
            var e_1, _a;
            return tslib__WEBPACK_IMPORTED_MODULE_0__["__generator"](this, function (_b) {
                switch (_b.label) {
                    case 0:
                        articlesObs = this.httpClient.get('/api/articles/');
                        return [4 /*yield*/, articlesObs.toPromise()];
                    case 1:
                        articles = _b.sent();
                        out = [];
                        _b.label = 2;
                    case 2:
                        _b.trys.push([2, 7, 8, 9]);
                        articles_1 = tslib__WEBPACK_IMPORTED_MODULE_0__["__values"](articles), articles_1_1 = articles_1.next();
                        _b.label = 3;
                    case 3:
                        if (!!articles_1_1.done) return [3 /*break*/, 6];
                        it = articles_1_1.value;
                        copy = it;
                        return [4 /*yield*/, Promise.all([
                                this.authService.getUser("" + it.createdBy),
                                this.getArticleContent(copy.uuid).toPromise()
                            ])];
                    case 4:
                        promises = _b.sent();
                        copy.createdBy = promises[0];
                        copy.content = promises[1];
                        out.push(copy);
                        _b.label = 5;
                    case 5:
                        articles_1_1 = articles_1.next();
                        return [3 /*break*/, 3];
                    case 6: return [3 /*break*/, 9];
                    case 7:
                        e_1_1 = _b.sent();
                        e_1 = { error: e_1_1 };
                        return [3 /*break*/, 9];
                    case 8:
                        try {
                            if (articles_1_1 && !articles_1_1.done && (_a = articles_1.return)) _a.call(articles_1);
                        }
                        finally { if (e_1) throw e_1.error; }
                        return [7 /*endfinally*/];
                    case 9:
                        console.log(out);
                        return [2 /*return*/, out];
                }
            });
        });
    };
    ArticleService.prototype.getArticleByUUID = function (uuid) {
        return this.httpClient.get("/api/articles/" + uuid);
    };
    ArticleService.prototype.createNewArticle = function (article, userToken) {
        return tslib__WEBPACK_IMPORTED_MODULE_0__["__awaiter"](this, void 0, void 0, function () {
            var httpOptions, uuid, content, title, categories, newArticle, _a;
            return tslib__WEBPACK_IMPORTED_MODULE_0__["__generator"](this, function (_b) {
                switch (_b.label) {
                    case 0:
                        httpOptions = {
                            headers: new _angular_common_http__WEBPACK_IMPORTED_MODULE_2__["HttpHeaders"]({
                                'Content-Type': 'application/json',
                                'Authorization': "Bearer " + userToken
                            })
                        };
                        uuid = article.uuid, content = article.content, title = article.title, categories = article.categories;
                        _a = {
                            uuid: uuid,
                            content: content,
                            title: title,
                            categories: categories
                        };
                        return [4 /*yield*/, this.authService.getUserUUID(userToken)];
                    case 1:
                        newArticle = (_a.createdBy = _b.sent(),
                            _a);
                        console.log("yes");
                        return [2 /*return*/, this.httpClient.post("/api/articles/", newArticle, httpOptions).toPromise()];
                }
            });
        });
    };
    // noinspection JSUnusedGlobalSymbols
    ArticleService.prototype.updateArticle = function (uuid, article, userToken) {
        var httpOptions = {
            headers: new _angular_common_http__WEBPACK_IMPORTED_MODULE_2__["HttpHeaders"]({
                'Content-Type': 'application/json',
                'Authorization': "Bearer " + userToken
            })
        };
        return this.httpClient.patch("/api/articles/" + uuid, article);
    };
    // noinspection JSUnusedGlobalSymbols
    ArticleService.prototype.deleteArticle = function (uuid, userToken) {
        return this.httpClient.delete("api/articles/" + uuid);
    };
    // noinspection JSUnusedGlobalSymbols
    ArticleService.prototype.getArticleContent = function (uuid) {
        return this.httpClient.get("/api/articles/content/" + uuid);
    };
    ArticleService.ctorParameters = function () { return [
        { type: _angular_common_http__WEBPACK_IMPORTED_MODULE_2__["HttpClient"] },
        { type: _auth_auth_service__WEBPACK_IMPORTED_MODULE_3__["AuthService"] }
    ]; };
    ArticleService = tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
        Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Injectable"])({
            providedIn: 'root'
        })
    ], ArticleService);
    return ArticleService;
}());



/***/ }),

/***/ "./src/app/services/auth/auth.service.ts":
/*!***********************************************!*\
  !*** ./src/app/services/auth/auth.service.ts ***!
  \***********************************************/
/*! exports provided: AuthService */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "AuthService", function() { return AuthService; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "./node_modules/tslib/tslib.es6.js");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm5/core.js");
/* harmony import */ var _angular_common_http__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @angular/common/http */ "./node_modules/@angular/common/fesm5/http.js");
/* harmony import */ var src_app_models_User__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! src/app/models/User */ "./src/app/models/User.ts");
/* harmony import */ var rxjs__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! rxjs */ "./node_modules/rxjs/_esm5/index.js");





var AuthService = /** @class */ (function () {
    function AuthService(httpClient) {
        this.httpClient = httpClient;
        this.currentUserToken_ = new rxjs__WEBPACK_IMPORTED_MODULE_4__["BehaviorSubject"]('');
        this.dummyUser = new src_app_models_User__WEBPACK_IMPORTED_MODULE_3__["User"]('', '');
        this.currentUser_ = new rxjs__WEBPACK_IMPORTED_MODULE_4__["BehaviorSubject"](this.dummyUser);
        this.currentUserUuid_ = new rxjs__WEBPACK_IMPORTED_MODULE_4__["BehaviorSubject"]('');
    }
    AuthService.prototype.login = function (user) {
        return tslib__WEBPACK_IMPORTED_MODULE_0__["__awaiter"](this, void 0, void 0, function () {
            var token, it;
            return tslib__WEBPACK_IMPORTED_MODULE_0__["__generator"](this, function (_a) {
                switch (_a.label) {
                    case 0:
                        token = this.httpClient.post('/api/auth/signin', user);
                        return [4 /*yield*/, token.toPromise()];
                    case 1:
                        it = _a.sent();
                        console.log("it.token: " + it.token);
                        this.currentUserToken_.next(it.token);
                        return [2 /*return*/, it];
                }
            });
        });
    };
    AuthService.prototype.register = function (user) {
        return this.httpClient.post('/api/auth/signup', user);
    };
    AuthService.prototype.requestUser = function (uuid) {
        return tslib__WEBPACK_IMPORTED_MODULE_0__["__awaiter"](this, void 0, void 0, function () {
            var userObservable, user;
            return tslib__WEBPACK_IMPORTED_MODULE_0__["__generator"](this, function (_a) {
                switch (_a.label) {
                    case 0:
                        userObservable = this.httpClient.get("/api/users/" + uuid);
                        return [4 /*yield*/, userObservable.toPromise()];
                    case 1:
                        user = _a.sent();
                        this.currentUser_.next(user);
                        return [2 /*return*/, user];
                }
            });
        });
    };
    AuthService.prototype.getUserUUID = function (token) {
        return tslib__WEBPACK_IMPORTED_MODULE_0__["__awaiter"](this, void 0, void 0, function () {
            var userUUIDObservable, uuid;
            return tslib__WEBPACK_IMPORTED_MODULE_0__["__generator"](this, function (_a) {
                switch (_a.label) {
                    case 0:
                        userUUIDObservable = this.httpClient.get("/api/auth/" + token);
                        return [4 /*yield*/, userUUIDObservable.toPromise()];
                    case 1:
                        uuid = _a.sent();
                        this.currentUserUuid_.next(uuid.uuid);
                        return [2 /*return*/, uuid];
                }
            });
        });
    };
    Object.defineProperty(AuthService.prototype, "userToken", {
        get: function () {
            return this.currentUserToken_.getValue();
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(AuthService.prototype, "userUUID", {
        get: function () {
            return this.currentUserUuid_.getValue();
        },
        enumerable: true,
        configurable: true
    });
    AuthService.prototype.getUser = function (uuid) {
        return tslib__WEBPACK_IMPORTED_MODULE_0__["__awaiter"](this, void 0, void 0, function () {
            var cUserVal;
            return tslib__WEBPACK_IMPORTED_MODULE_0__["__generator"](this, function (_a) {
                switch (_a.label) {
                    case 0:
                        cUserVal = this.currentUser_.getValue();
                        if (!(cUserVal == this.dummyUser || cUserVal.username == '')) return [3 /*break*/, 2];
                        return [4 /*yield*/, this.requestUser(uuid)];
                    case 1:
                        _a.sent();
                        _a.label = 2;
                    case 2: return [2 /*return*/, this.currentUser_.getValue()];
                }
            });
        });
    };
    AuthService.ctorParameters = function () { return [
        { type: _angular_common_http__WEBPACK_IMPORTED_MODULE_2__["HttpClient"] }
    ]; };
    AuthService = tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
        Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Injectable"])({
            providedIn: 'root'
        })
    ], AuthService);
    return AuthService;
}());



/***/ }),

/***/ "./src/app/services/comments/comments.service.ts":
/*!*******************************************************!*\
  !*** ./src/app/services/comments/comments.service.ts ***!
  \*******************************************************/
/*! exports provided: CommentsService */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "CommentsService", function() { return CommentsService; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "./node_modules/tslib/tslib.es6.js");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm5/core.js");
/* harmony import */ var _angular_common_http__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @angular/common/http */ "./node_modules/@angular/common/fesm5/http.js");



var CommentsService = /** @class */ (function () {
    function CommentsService(httpClient) {
        this.httpClient = httpClient;
        this.commentsEndpoint = '/api/articles/comments';
    }
    CommentsService.prototype.getCommentsForArticle = function (articleUUID) {
        return this.httpClient.get(this.commentsEndpoint + "/" + articleUUID);
    };
    CommentsService.prototype.deleteComment = function (commentUUID) {
        return this.httpClient.delete(this.commentsEndpoint + "/" + commentUUID);
    };
    CommentsService.prototype.createComment = function (comment) {
        return this.httpClient.post("" + this.commentsEndpoint, comment);
    };
    CommentsService.ctorParameters = function () { return [
        { type: _angular_common_http__WEBPACK_IMPORTED_MODULE_2__["HttpClient"] }
    ]; };
    CommentsService = tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
        Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Injectable"])({
            providedIn: 'root'
        })
    ], CommentsService);
    return CommentsService;
}());



/***/ }),

/***/ "./src/environments/environment.ts":
/*!*****************************************!*\
  !*** ./src/environments/environment.ts ***!
  \*****************************************/
/*! exports provided: environment */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "environment", function() { return environment; });
// This file can be replaced during build by using the `fileReplacements` array.
// `ng build --prod` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.
var environment = {
    production: false
};
/*
 * For easier debugging in development mode, you can import the following file
 * to ignore zone related error stack frames such as `zone.run`, `zoneDelegate.invokeTask`.
 *
 * This import should be commented out in production mode because it will have a negative impact
 * on performance if an error is thrown.
 */
// import 'zone.js/dist/zone-error';  // Included with Angular CLI.


/***/ }),

/***/ "./src/main.ts":
/*!*********************!*\
  !*** ./src/main.ts ***!
  \*********************/
/*! no exports provided */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm5/core.js");
/* harmony import */ var _angular_platform_browser_dynamic__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/platform-browser-dynamic */ "./node_modules/@angular/platform-browser-dynamic/fesm5/platform-browser-dynamic.js");
/* harmony import */ var _app_app_module__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ./app/app.module */ "./src/app/app.module.ts");
/* harmony import */ var _environments_environment__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ./environments/environment */ "./src/environments/environment.ts");




if (_environments_environment__WEBPACK_IMPORTED_MODULE_3__["environment"].production) {
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_0__["enableProdMode"])();
}
Object(_angular_platform_browser_dynamic__WEBPACK_IMPORTED_MODULE_1__["platformBrowserDynamic"])().bootstrapModule(_app_app_module__WEBPACK_IMPORTED_MODULE_2__["AppModule"])
    .catch(function (err) { return console.error(err); });


/***/ }),

/***/ 0:
/*!***************************!*\
  !*** multi ./src/main.ts ***!
  \***************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

module.exports = __webpack_require__(/*! /home/lucyagamaite/blogify/src/blogify/frontend/src/main.ts */"./src/main.ts");


/***/ })

},[[0,"runtime","vendor"]]]);
//# sourceMappingURL=main-es5.js.map